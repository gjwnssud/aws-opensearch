package com.hzn.awsopensearch.service.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzn.awsopensearch.dto.ResponseDto;
import com.hzn.awsopensearch.dto.index.AliasRequestRequestDto;
import com.hzn.awsopensearch.dto.index.CmtyNttInfoDto;
import com.hzn.awsopensearch.dto.index.CmtyNttRequestDto;
import com.hzn.awsopensearch.dto.index.IndexRequestDto;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequestDto;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequestDto.Action;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequestDto.Action.Add;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequestDto.Aggregations;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchResponseDto;
import com.hzn.awsopensearch.enums.OpenSearchEndpoint;
import com.hzn.awsopensearch.enums.Status;
import com.hzn.awsopensearch.exception.AwsOpensearchException;
import com.hzn.awsopensearch.mapper.index.IndexMapper;
import com.hzn.awsopensearch.util.Async;
import com.hzn.awsopensearch.util.RetryHandler;
import com.hzn.awsopensearch.util.http.HttpClient;
import com.hzn.awsopensearch.util.http.HttpResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Service
@RequiredArgsConstructor
public class IndexService {
	@Value ("${cloud.aws.opensearch.domain}")
	private String openSearchDomain;
	@Value ("${cloud.aws.opensearch.master.id}")
	private String masterId;
	@Value ("${cloud.aws.opensearch.master.password}")
	private String masterPassword;
	private final ObjectMapper objectMapper;
	private final IndexMapper indexMapper;
	private final ConcurrentHashMap<String, String> bulkMap = new ConcurrentHashMap<> ();
	private final ConcurrentHashMap<String, String> upsertMap = new ConcurrentHashMap<> ();

	public ResponseDto<Map<String, Object>> createIndex (IndexRequestDto indexRequestDto) throws Exception {
		String indexSettings = getIndexSettings ();
		return RetryHandler.retry (() -> {
			HttpResponse<Map<String, Object>> httpResponse = HttpClient.builder ()
			                                                           .url (openSearchDomain + "/" + indexRequestDto.getIndexName ())
			                                                           .contentType (MediaType.APPLICATION_JSON_VALUE)
			                                                           .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
			                                                           .addParametersFromObject (indexSettings)
			                                                           .put ()
			                                                           .getHttpResponseByMap ();
			if (httpResponse.getCode () != 200) {
				throw new AwsOpensearchException (httpResponse.getMessage ());
			} else {
				RetryHandler.resetRetryCount ();
			}
			return ResponseDto.from (httpResponse);
		}, 3);
	}

	public ResponseDto<Map<String, Object>> deleteIndex (IndexRequestDto indexRequestDto) {
		return ResponseDto.from (HttpClient.builder ()
		                                   .url (openSearchDomain + "/" + indexRequestDto.getIndexName ())
		                                   .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                                   .delete ()
		                                   .getHttpResponseByMap ());
	}

	public ResponseDto<Map<String, Object>> setAlias (AliasRequestRequestDto aliasRequestDto) {
		return ResponseDto.from (HttpClient.builder ()
		                                   .url (openSearchDomain + OpenSearchEndpoint._ALIASES.getPath ())
		                                   .contentType (MediaType.APPLICATION_JSON_VALUE)
		                                   .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                                   .addParametersFromObject (OpenSearchAliasRequestDto.builder ()
		                                                                                      .actions (List.of (Action.builder ()
		                                                                                                               .add (Add.builder ()
		                                                                                                                        .index (aliasRequestDto.getIndexName ())
		                                                                                                                        .alias (aliasRequestDto.getAliasName ())
		                                                                                                                        .build ())
		                                                                                                               .build ()))
		                                                                                      .build ())
		                                   .post ()
		                                   .getHttpResponseByMap ());
	}

	public ResponseDto<String> bulkIndexing (IndexRequestDto indexRequestDto) {
		String indexName = indexRequestDto.getIndexName ();
		if (bulkMap.containsKey (indexName)) {
			return ResponseDto.of (Status.FAIL.getCode (), "[" + indexName + "] bulk indexing 작업이 진행 중 입니다.");
		}

		bulkMap.put (indexName, "running");
		final int[] pageNumber = {1};
		CmtyNttRequestDto cmtyNttRequest = CmtyNttRequestDto.builder ().pageNumber (pageNumber[0]).pageSize (50).build ();
		final List<CmtyNttInfoDto> cmtyNttInfoDtoList = new ArrayList<> ();
		Async.scheduleWithFixedDelay (() -> {
			cmtyNttInfoDtoList.clear ();
			cmtyNttInfoDtoList.addAll (indexMapper.getCmtyNttInfoList (cmtyNttRequest));
			cmtyNttRequest.setPageNumber (++pageNumber[0]);
			return !cmtyNttInfoDtoList.isEmpty ();
		}, () -> doBulkIndexing (indexName, cmtyNttInfoDtoList), () -> bulkMap.remove (indexName));

		return ResponseDto.of (Status.OK.getCode (), "[" + indexName + "] bulk indexing 요청 완료.");
	}

	private void doBulkIndexing (String indexName, List<CmtyNttInfoDto> cmtyNttInfoDtoList) {
		StringBuilder sb = new StringBuilder ();
		cmtyNttInfoDtoList.forEach (nttInfo -> {
			try {
				sb.append ("{\"index\" : {\"_index\" : \"").append (indexName).append ("\", \"_id\" : \"").append (nttInfo.getCmtyNttSn ()).append ("\"}}\n");
				sb.append (objectMapper.writeValueAsString (nttInfo)).append ("\n");
			} catch (JsonProcessingException e) {
				throw new AwsOpensearchException (e.getMessage ());
			}
		});

		RetryHandler.retry (() -> {
			HttpResponse<Map<String, Object>> httpResponse = HttpClient.builder ()
			                                                           .url (openSearchDomain + "/" + indexName + OpenSearchEndpoint._BULK.getPath ())
			                                                           .contentType (MediaType.APPLICATION_JSON_VALUE)
			                                                           .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
			                                                           .requestBody (sb.toString ())
			                                                           .post ()
			                                                           .getHttpResponseByMap ();
			if (httpResponse.getCode () != 200) {
				throw new AwsOpensearchException (httpResponse.getMessage ());
			} else {
				RetryHandler.resetRetryCount ();
			}
		}, 3);
	}

	public ResponseDto<String> upsertIndexing (IndexRequestDto indexRequestDto) {
		String indexName = indexRequestDto.getIndexName ();
		if (upsertMap.containsKey (indexName)) {
			return ResponseDto.of (Status.FAIL.getCode (), "[" + indexName + "] upsert indexing 작업이 진행 중 입니다.");
		}

		upsertMap.put (indexName, "running");
		// 인덱싱 된 데이터의 sysRegistDt, sysUpdtDt 최대값 조회
		OpenSearchResponseDto.Aggregations maxDateAggregations;
		try {
			maxDateAggregations = getMaxDateAggregations (indexName);
		} catch (IOException e) {
			throw new AwsOpensearchException (e.getMessage ());
		}
		// 데이터 조회
		final int[] pageNumber = {1};
		CmtyNttRequestDto cmtyNttRequest = maxDateAggregations.toCmtyNttRequest ();
		cmtyNttRequest.setPageSize (50);
		cmtyNttRequest.setPageNumber (pageNumber[0]);
		final List<Integer> nttSnList = new ArrayList<> ();
		Async.scheduleWithFixedDelay (() -> {
			                              nttSnList.clear ();
			                              nttSnList.addAll (indexMapper.getIndexableNttSnList (cmtyNttRequest));
			                              cmtyNttRequest.setPageNumber (++pageNumber[0]);
			                              return !nttSnList.isEmpty ();
		                              }, () -> doUpsertIndexing (indexName, indexMapper.getCmtyNttInfoList (CmtyNttRequestDto.builder ().cmtyNttSnList (nttSnList).build ())),
		                              () -> upsertMap.remove (indexName));

		return ResponseDto.of (Status.OK.getCode (), "[" + indexName + "] upsert indexing 요청 완료.");
	}

	private void doUpsertIndexing (String indexName, List<CmtyNttInfoDto> cmtyNttInfoDtoList) {
		StringBuilder sb = new StringBuilder ();
		cmtyNttInfoDtoList.forEach (nttInfo -> {
			try {
				sb.append (objectMapper.writeValueAsString (nttInfo));
			} catch (JsonProcessingException e) {
				throw new AwsOpensearchException (e.getMessage ());
			}

			RetryHandler.retry (() -> {
				HttpResponse<Map<String, Object>> httpResponse = HttpClient.builder ()
				                                                           .url (openSearchDomain + "/" + indexName + OpenSearchEndpoint._DOC.getPath () + "/"
						                                                                 + nttInfo.getCmtyNttSn ())
				                                                           .contentType (MediaType.APPLICATION_JSON_VALUE)
				                                                           .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
				                                                           .requestBody (sb.toString ())
				                                                           .put ()
				                                                           .getHttpResponseByMap ();
				if (httpResponse.getCode () != 200) {
					throw new AwsOpensearchException (httpResponse.getMessage ());
				} else {
					RetryHandler.resetRetryCount ();
				}
			}, 3);

			sb.setLength (0);
		});
	}

	private OpenSearchResponseDto.Aggregations getMaxDateAggregations (String indexName) throws IOException {
		OpenSearchRequestDto nttRequest = OpenSearchRequestDto.builder ()
		                                                      .size (0)
		                                                      .aggs (Aggregations.builder ()
		                                                                         .cmtyNttMaxSysRegistDt (Map.of ("max", Map.of ("field", "sysRegistDt")))
		                                                                         .cmtyNttMaxSysUpdtDt (Map.of ("max", Map.of ("field", "sysUpdtDt")))
		                                                                         .build ())
		                                                      .build ();
		OpenSearchResponseDto nttResponse = getAggregations (indexName, nttRequest);
		OpenSearchResponseDto.Aggregations nttAggregations = nttResponse.getAggregations ();

		OpenSearchRequestDto answerRequest = OpenSearchRequestDto.builder ()
		                                                         .size (0)
		                                                         .aggs (Aggregations.builder ()
		                                                                            .cmtyNttAnswersMaxSysRegistDt (
				                                                                            Map.of ("max", Map.of ("field", "cmtyNttAnswers.sysRegistDt")))
		                                                                            .cmtyNttAnswersMaxSysUpdtDt (
				                                                                            Map.of ("max", Map.of ("field", "cmtyNttAnswers.sysUpdtDt")))
		                                                                            .build ())
		                                                         .build ();
		OpenSearchResponseDto answerResponse = getAggregations (indexName, answerRequest);
		OpenSearchResponseDto.Aggregations answerAggregations = answerResponse.getAggregations ();

		OpenSearchRequestDto nttBlckgRequest = OpenSearchRequestDto.builder ()
		                                                           .size (0)
		                                                           .aggs (Aggregations.builder ()
		                                                                              .nttBlckgInfoMaxSysRegistDt (
				                                                                              Map.of ("max", Map.of ("field", "nttBlckgInfo.sysRegistDt")))
		                                                                              .nttBlckgInfoMaxSysUpdtDt (
				                                                                              Map.of ("max", Map.of ("field", "nttBlckgInfo.sysUpdtDt")))
		                                                                              .build ())
		                                                           .build ();
		OpenSearchResponseDto nttBlckgResponse = getAggregations (indexName, nttBlckgRequest);
		OpenSearchResponseDto.Aggregations nttBlckgAggregations = nttBlckgResponse.getAggregations ();

		OpenSearchRequestDto cmtyFrendBlckgRequest = OpenSearchRequestDto.builder ()
		                                                                 .size (0)
		                                                                 .aggs (Aggregations.builder ()
		                                                                                    .cmtyFrendBlckgInfoMaxSysRegistDt (
				                                                                                    Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo.sysRegistDt")))
		                                                                                    .cmtyFrendBlckgInfoMaxSysUpdtDt (
				                                                                                    Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo.sysUpdtDt")))
		                                                                                    .build ())
		                                                                 .build ();
		OpenSearchResponseDto cmtyFrendBlckgResponse = getAggregations (indexName, cmtyFrendBlckgRequest);
		OpenSearchResponseDto.Aggregations cmtyFrendBlckgAggregations = cmtyFrendBlckgResponse.getAggregations ();

		OpenSearchRequestDto cmtyFrendBlckg2Request = OpenSearchRequestDto.builder ()
		                                                                  .size (0)
		                                                                  .aggs (Aggregations.builder ()
		                                                                                     .cmtyFrendBlckgInfo2MaxSysRegistDt (
				                                                                                     Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo2.sysRegistDt")))
		                                                                                     .cmtyFrendBlckgInfo2MaxSysUpdtDt (
				                                                                                     Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo2.sysUpdtDt")))
		                                                                                     .build ())
		                                                                  .build ();
		OpenSearchResponseDto cmtyFrendBlckg2Response = getAggregations (indexName, cmtyFrendBlckg2Request);
		OpenSearchResponseDto.Aggregations cmtyFrendBlckg2Aggregations = cmtyFrendBlckg2Response.getAggregations ();

		return OpenSearchResponseDto.Aggregations.merge (nttAggregations, answerAggregations, nttBlckgAggregations, cmtyFrendBlckgAggregations,
		                                                 cmtyFrendBlckg2Aggregations);
	}

	private OpenSearchResponseDto getAggregations (String indexName, OpenSearchRequestDto openSearchRequestDto) throws IOException {
		HttpResponse<OpenSearchResponseDto> httpResponse = HttpClient.builder ()
		                                                             .url (openSearchDomain + "/" + indexName + OpenSearchEndpoint._SEARCH.getPath ())
		                                                             .contentType (MediaType.APPLICATION_JSON_VALUE)
		                                                             .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                                                             .addParametersFromObject (openSearchRequestDto)
		                                                             .post ()
		                                                             .getHttpResponseByClass (OpenSearchResponseDto.class);
		if (httpResponse.getCode () != 200) {
			throw new AwsOpensearchException (httpResponse.getMessage ());
		}
		return httpResponse.getData ();
	}

	private String getIndexSettings () throws IOException {
		try (BufferedReader br = new BufferedReader (
				new InputStreamReader (new ClassPathResource ("opensearch/searchIndexSettings.json").getInputStream (), StandardCharsets.UTF_8))) {
			String line;
			StringBuilder sb = new StringBuilder ();
			while ((line = br.readLine ()) != null) {
				sb.append (line);
			}
			return sb.toString ();
		}
	}

	private String getAuthorization () {
		return "Basic " + Base64.getEncoder ().encodeToString ((masterId + ":" + masterPassword).getBytes (StandardCharsets.UTF_8));
	}

	private Map<String, List<String>> getOldIndexName (String indexName) throws IOException {
		HttpResponse<Map<String, Object>> httpResponse = HttpClient.builder ()
		                                                           .url (openSearchDomain + OpenSearchEndpoint._ALL.getPath ())
		                                                           .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                                                           .get ()
		                                                           .getHttpResponseByMap ();
		if (httpResponse.getCode () != 200) {
			throw new AwsOpensearchException (httpResponse.getMessage ());
		}

		Map<String, List<String>> resultMap = new HashMap<> ();
		httpResponse.getData ().forEach ((k, v) -> extractIndexName (resultMap, k, indexName));
		return resultMap;
	}

	private void extractIndexName (Map<String, List<String>> resultMap, String k, String indexName) {
		if (k.contains (indexName)) {
			List<String> indexNameList = resultMap.get (indexName);
			if (indexNameList == null) {
				indexNameList = new ArrayList<> ();
				indexNameList.add (k);
				resultMap.put (indexName, indexNameList);
			} else {
				indexNameList.add (k);
			}
		}
	}
}
