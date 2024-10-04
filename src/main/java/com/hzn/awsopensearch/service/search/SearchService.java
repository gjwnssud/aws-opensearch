package com.hzn.awsopensearch.service.search;

import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.OpenSearchRequestDtoBuilder;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Query;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Query.Bool;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Query.Bool.BoolBuilder;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Query.Bool.Must;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Query.MultiMatch;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.SortProperties;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Type;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchResponseDto;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchResponseDto.Hits.SubHits;
import com.hzn.awsopensearch.dto.search.AutocompleteRequestDto;
import com.hzn.awsopensearch.dto.search.SearchRequestDto;
import com.hzn.awsopensearch.util.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 24.
 */
@Service
@RequiredArgsConstructor
public class SearchService {
	@Value ("${cloud.aws.opensearch.domain}")
	private String openSearchDomain;
	@Value ("${cloud.aws.opensearch.master.id}")
	private String masterId;
	@Value ("${cloud.aws.opensearch.master.password}")
	private String masterPassword;

	public List<String> autocomplete (AutocompleteRequestDto autocompleteRequestDto) {
		String keyword = autocompleteRequestDto.getKeyword ();
		BoolBuilder boolBuilder = Bool.builder ();
		List<Must> mustList = new ArrayList<> ();
		mustList.add (Must.builder ()
		                  .multi_match (MultiMatch.builder ()
		                                          .query (keyword)
		                                          .fields (List.of ("nttSj.nori", "nttCn.nori", "cmtyNttAnswers.nttAnswerCn.nori"))
		                                          .type ("phrase_prefix")
		                                          .build ())
		                  .build ());
		mustList.add (Must.builder ().term (Map.of ("prmbrshCntntsAt.keyword", "N")).build ());
		mustList.add (Must.builder ().term (Map.of ("nttOthbcScopeCode.keyword", "PUBL")).build ());
		mustList.add (Must.builder ().term (Map.of ("deleteAt.keyword", "N")).build ());
		boolBuilder.must (mustList);

		OpenSearchResponseDto openSearchResponseDto = search (OpenSearchRequestDto.builder ()
		                                                                          .indexName (autocompleteRequestDto.getIndexName ())
		                                                                          .type (Type._SEARCH)
		                                                                          .from (0)
		                                                                          .size (20)
		                                                                          .query (Query.builder ().bool (boolBuilder.build ()).build ())
		                                                                          .build ());
		if (openSearchResponseDto.getError () != null || openSearchResponseDto.getHits ().getTotal ().getValue () == 0) {
			return List.of ();
		}

		Stream<String> combinedStream = openSearchResponseDto.getHits ().getHits ().stream ().flatMap (hit -> {
			Map<String, Object> source = hit.get_source ();
			Stream<String> nttSjStream = Stream.ofNullable ((String) source.get ("nttSj"));
			Stream<String> nttCnStream = Stream.ofNullable ((String) source.get ("nttCn"));
			if (source.get ("cmtyNttAnswers") != null) {
				Stream<String> nttAnswerCnStream = ((List<Map<String, Object>>) source.get ("cmtyNttAnswers")).stream ().map (o -> (String) o.get ("nttAnswerCn"));
				return Stream.of (nttSjStream, nttCnStream, nttAnswerCnStream).flatMap (s -> s);
			}
			return Stream.of (nttSjStream, nttCnStream).flatMap (s -> s);
		});

		return combinedStream.flatMap (s -> Arrays.stream (s.split ("\\s+")))
		                     .filter (s -> s.toLowerCase ().startsWith (keyword.toLowerCase ()))
		                     .distinct ()
		                     .sorted ()
		                     .limit (10)
		                     .toList ();
	}

	public List<Map<String, Object>> nttSearch (SearchRequestDto searchRequestDto) {
		BoolBuilder boolBuilder = Bool.builder ();
		List<Must> mustList = new ArrayList<> ();
		mustList.add (Must.builder ().term (Map.of ("deleteAt.keyword", "N")).build ());
		mustList.add (Must.builder ().term (Map.of ("nttOthbcScopeCode.keyword", "PUBL")).build ());

		String keyword = searchRequestDto.getKeyword ();
		if (!ObjectUtils.isEmpty (keyword)) {
			mustList.add (Must.builder ().multi_match (MultiMatch.builder ().query (keyword).fields (List.of ("nttSj", "nttCn")).type ("phrase").build ()).build ());
		}

		OpenSearchRequestDtoBuilder openSearchRequestBuilder = OpenSearchRequestDto.builder ();
		openSearchRequestBuilder.indexName (searchRequestDto.getIndexName ())
		                        .type (Type._SEARCH)
		                        .from (0)
		                        .size (searchRequestDto.getSize ())
		                        .sort (List.of (Map.of ("cmtyNttSn", SortProperties.builder ().order ("desc").build ())))
		                        .query (Query.builder ().bool (boolBuilder.must (mustList).build ()).build ());
		OpenSearchResponseDto openSearchResponseDto = search (openSearchRequestBuilder.build ());
		if (openSearchResponseDto.getError () != null || openSearchResponseDto.getHits ().getTotal ().getValue () == 0) {
			return List.of ();
		}
		return openSearchResponseDto.getHits ().getHits ().stream ().map (SubHits::get_source).toList ();
	}

	private OpenSearchResponseDto search (OpenSearchRequestDto openSearchRequestDto) {
		return HttpClient.builder ()
		                 .url (openSearchDomain + "/" + openSearchRequestDto.getIndexName () + openSearchRequestDto.getType ().getPath ())
		                 .contentType (MediaType.APPLICATION_JSON_VALUE)
		                 .addHeader (HttpHeaders.AUTHORIZATION,
		                             "Basic " + Base64.getEncoder ().encodeToString ((masterId + ":" + masterPassword).getBytes (StandardCharsets.UTF_8)))
		                 .addParametersFromObject (openSearchRequestDto)
		                 .post ()
		                 .getHttpResponseByClass (OpenSearchResponseDto.class)
		                 .getData ();
	}
}
