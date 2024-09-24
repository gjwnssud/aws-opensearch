package com.hzn.awsopensearch.service.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.dto.index.AliasRequest;
import com.hzn.awsopensearch.dto.index.IndexRequest;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequest;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequest.Action;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequest.Action.Add;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequest;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchRequest.Aggregations;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchResponse;
import com.hzn.awsopensearch.enums.OpenSearchEndpoint;
import com.hzn.awsopensearch.enums.Status;
import com.hzn.awsopensearch.exception.AwsOpensearchException;
import com.hzn.awsopensearch.mapper.index.IndexMapper;
import com.hzn.awsopensearch.util.Async;
import com.hzn.awsopensearch.util.http.HttpClient;
import com.hzn.awsopensearch.util.RetryHandler;
import com.hzn.awsopensearch.vo.index.CmtyNttInfo;
import com.hzn.awsopensearch.vo.index.CmtyNttRequest;
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
@SuppressWarnings ({"unchecked"})
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

	public Response<Map<String, Object>> createIndex (IndexRequest indexRequest) throws Exception {
		String indexSettings = getIndexSettings ();
		return RetryHandler.retry (() -> {
			Response<Map<String, Object>> response = HttpClient.builder ()
			                                                   .url (openSearchDomain + "/" + indexRequest.getIndexName ())
			                                                   .contentType (MediaType.APPLICATION_JSON_VALUE)
			                                                   .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
			                                                   .addParametersFromObject (indexSettings)
			                                                   .put ()
			                                                   .getResponseByMap ();
			if (response.getCode () != 200) {
				throw new AwsOpensearchException (response.getMessage ());
			} else {
				RetryHandler.resetRetryCount ();
			}
			return response;
		}, 3);
	}

	public Response<Map<String, Object>> deleteIndex (IndexRequest indexRequest) {
		return HttpClient.builder ()
		                 .url (openSearchDomain + "/" + indexRequest.getIndexName ())
		                 .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                 .delete ()
		                 .getResponseByMap ();
	}

	public Response<Map<String, Object>> setAlias (AliasRequest aliasRequest) {
		return HttpClient.builder ()
		                 .url (openSearchDomain + OpenSearchEndpoint._ALIASES.getPath ())
		                 .contentType (MediaType.APPLICATION_JSON_VALUE)
		                 .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                 .addParametersFromObject (OpenSearchAliasRequest.builder ()
		                                                                 .actions (List.of (Action.builder ()
		                                                                                          .add (Add.builder ()
		                                                                                                   .index (aliasRequest.getIndexName ())
		                                                                                                   .alias (aliasRequest.getAliasName ())
		                                                                                                   .build ())
		                                                                                          .build ()))
		                                                                 .build ())
		                 .post ()
		                 .getResponseByMap ();
	}

	public Response<String> bulkIndexing (IndexRequest indexRequest) {
		String indexName = indexRequest.getIndexName ();
		if (bulkMap.containsKey (indexName)) {
			return Response.of (Status.FAIL.getCode (), "[" + indexName + "] bulk indexing 작업이 진행 중 입니다.");
		}

		bulkMap.put (indexName, "running");
		final int[] pageNumber = {1};
		CmtyNttRequest cmtyNttRequest = CmtyNttRequest.builder ().pageNumber (pageNumber[0]).pageSize (50).build ();
		final List<CmtyNttInfo> cmtyNttInfoList = new ArrayList<> ();
		Async.scheduleWithFixedDelay (() -> {
			cmtyNttInfoList.clear ();
			cmtyNttInfoList.addAll (indexMapper.getCmtyNttInfoList (cmtyNttRequest));
			cmtyNttRequest.setPageNumber (++pageNumber[0]);
			return !cmtyNttInfoList.isEmpty ();
		}, () -> doBulkIndexing (indexName, cmtyNttInfoList), () -> bulkMap.remove (indexName));

		return Response.of (Status.OK.getCode (), "[" + indexName + "] bulk indexing 요청 완료.");
	}

	private void doBulkIndexing (String indexName, List<CmtyNttInfo> cmtyNttInfoList) {
		StringBuilder sb = new StringBuilder ();
		cmtyNttInfoList.forEach (nttInfo -> {
			try {
				sb.append ("{\"index\" : {\"_index\" : \"").append (indexName).append ("\", \"_id\" : \"").append (nttInfo.getCmtyNttSn ()).append ("\"}}\n");
				sb.append (objectMapper.writeValueAsString (nttInfo)).append ("\n");
			} catch (JsonProcessingException e) {
				throw new AwsOpensearchException (e.getMessage ());
			}
		});

		RetryHandler.retry (() -> {
			Response<Map<String, Object>> response = HttpClient.builder ()
			                                                   .url (openSearchDomain + "/" + indexName + OpenSearchEndpoint._BULK.getPath ())
			                                                   .contentType (MediaType.APPLICATION_JSON_VALUE)
			                                                   .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
			                                                   .setRequestBody (sb.toString ())
			                                                   .post ()
			                                                   .getResponseByMap ();
			if (response.getCode () != 200) {
				throw new AwsOpensearchException (response.getMessage ());
			} else {
				RetryHandler.resetRetryCount ();
			}
		}, 3);
	}

	public Response<String> upsertIndexing (IndexRequest indexRequest) {
		String indexName = indexRequest.getIndexName ();
		if (upsertMap.containsKey (indexName)) {
			return Response.of (Status.FAIL.getCode (), "[" + indexName + "] upsert indexing 작업이 진행 중 입니다.");
		}

		upsertMap.put (indexName, "running");
		// 인덱싱 된 데이터의 sysRegistDt, sysUpdtDt 최대값 조회
		OpenSearchResponse.Aggregations maxDateAggregations;
		try {
			maxDateAggregations = getMaxDateAggregations (indexName);
		} catch (IOException e) {
			throw new AwsOpensearchException (e.getMessage ());
		}
		// 데이터 조회
		final int[] pageNumber = {1};
		CmtyNttRequest cmtyNttRequest = maxDateAggregations.toCmtyNttRequest ();
		cmtyNttRequest.setPageSize (50);
		cmtyNttRequest.setPageNumber (pageNumber[0]);
		final List<Integer> nttSnList = new ArrayList<> ();
		Async.scheduleWithFixedDelay (() -> {
			                              nttSnList.clear ();
			                              nttSnList.addAll (indexMapper.getIndexableNttSnList (cmtyNttRequest));
			                              cmtyNttRequest.setPageNumber (++pageNumber[0]);
			                              return !nttSnList.isEmpty ();
		                              }, () -> doUpsertIndexing (indexName, indexMapper.getCmtyNttInfoList (CmtyNttRequest.builder ().cmtyNttSnList (nttSnList).build ())),
		                              () -> upsertMap.remove (indexName));

		return Response.of (Status.OK.getCode (), "[" + indexName + "] upsert indexing 요청 완료.");
	}

	private void doUpsertIndexing (String indexName, List<CmtyNttInfo> cmtyNttInfoList) {
		StringBuilder sb = new StringBuilder ();
		cmtyNttInfoList.forEach (nttInfo -> {
			try {
				sb.append (objectMapper.writeValueAsString (nttInfo));
			} catch (JsonProcessingException e) {
				throw new AwsOpensearchException (e.getMessage ());
			}

			RetryHandler.retry (() -> {
				Response<Map<String, Object>> response = HttpClient.builder ()
				                                                   .url (openSearchDomain + "/" + indexName + OpenSearchEndpoint._DOC.getPath () + "/"
						                                                         + nttInfo.getCmtyNttSn ())
				                                                   .contentType (MediaType.APPLICATION_JSON_VALUE)
				                                                   .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
				                                                   .setRequestBody (sb.toString ())
				                                                   .put ()
				                                                   .getResponseByMap ();
				if (response.getCode () != 200) {
					throw new AwsOpensearchException (response.getMessage ());
				} else {
					RetryHandler.resetRetryCount ();
				}
			}, 3);

			sb.setLength (0);
		});
	}

	private OpenSearchResponse.Aggregations getMaxDateAggregations (String indexName) throws IOException {
		OpenSearchRequest nttRequest = OpenSearchRequest.builder ()
		                                                .size (0)
		                                                .aggs (Aggregations.builder ()
		                                                                   .cmtyNttMaxSysRegistDt (Map.of ("max", Map.of ("field", "sysRegistDt")))
		                                                                   .cmtyNttMaxSysUpdtDt (Map.of ("max", Map.of ("field", "sysUpdtDt")))
		                                                                   .build ())
		                                                .build ();
		OpenSearchResponse nttResponse = getAggregations (indexName, nttRequest);
		OpenSearchResponse.Aggregations nttAggregations = nttResponse.getAggregations ();

		OpenSearchRequest answerRequest = OpenSearchRequest.builder ()
		                                                   .size (0)
		                                                   .aggs (Aggregations.builder ()
		                                                                      .cmtyNttAnswersMaxSysRegistDt (
				                                                                      Map.of ("max", Map.of ("field", "cmtyNttAnswers.sysRegistDt")))
		                                                                      .cmtyNttAnswersMaxSysUpdtDt (Map.of ("max", Map.of ("field", "cmtyNttAnswers.sysUpdtDt")))
		                                                                      .build ())
		                                                   .build ();
		OpenSearchResponse answerResponse = getAggregations (indexName, answerRequest);
		OpenSearchResponse.Aggregations answerAggregations = answerResponse.getAggregations ();

		OpenSearchRequest nttBlckgRequest = OpenSearchRequest.builder ()
		                                                     .size (0)
		                                                     .aggs (Aggregations.builder ()
		                                                                        .nttBlckgInfoMaxSysRegistDt (Map.of ("max", Map.of ("field", "nttBlckgInfo.sysRegistDt")))
		                                                                        .nttBlckgInfoMaxSysUpdtDt (Map.of ("max", Map.of ("field", "nttBlckgInfo.sysUpdtDt")))
		                                                                        .build ())
		                                                     .build ();
		OpenSearchResponse nttBlckgResponse = getAggregations (indexName, nttBlckgRequest);
		OpenSearchResponse.Aggregations nttBlckgAggregations = nttBlckgResponse.getAggregations ();

		OpenSearchRequest cmtyFrendBlckgRequest = OpenSearchRequest.builder ()
		                                                           .size (0)
		                                                           .aggs (Aggregations.builder ()
		                                                                              .cmtyFrendBlckgInfoMaxSysRegistDt (
				                                                                              Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo.sysRegistDt")))
		                                                                              .cmtyFrendBlckgInfoMaxSysUpdtDt (
				                                                                              Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo.sysUpdtDt")))
		                                                                              .build ())
		                                                           .build ();
		OpenSearchResponse cmtyFrendBlckgResponse = getAggregations (indexName, cmtyFrendBlckgRequest);
		OpenSearchResponse.Aggregations cmtyFrendBlckgAggregations = cmtyFrendBlckgResponse.getAggregations ();

		OpenSearchRequest cmtyFrendBlckg2Request = OpenSearchRequest.builder ()
		                                                            .size (0)
		                                                            .aggs (Aggregations.builder ()
		                                                                               .cmtyFrendBlckgInfo2MaxSysRegistDt (
				                                                                               Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo2.sysRegistDt")))
		                                                                               .cmtyFrendBlckgInfo2MaxSysUpdtDt (
				                                                                               Map.of ("max", Map.of ("field", "cmtyFrendBlckgInfo2.sysUpdtDt")))
		                                                                               .build ())
		                                                            .build ();
		OpenSearchResponse cmtyFrendBlckg2Response = getAggregations (indexName, cmtyFrendBlckg2Request);
		OpenSearchResponse.Aggregations cmtyFrendBlckg2Aggregations = cmtyFrendBlckg2Response.getAggregations ();

		return OpenSearchResponse.Aggregations.merge (nttAggregations, answerAggregations, nttBlckgAggregations, cmtyFrendBlckgAggregations, cmtyFrendBlckg2Aggregations);
	}

	private OpenSearchResponse getAggregations (String indexName, OpenSearchRequest openSearchRequest) throws IOException {
		Response<OpenSearchResponse> response = HttpClient.builder ()
		                                                  .url (openSearchDomain + "/" + indexName + OpenSearchEndpoint._SEARCH.getPath ())
		                                                  .contentType (MediaType.APPLICATION_JSON_VALUE)
		                                                  .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                                                  .addParametersFromObject (openSearchRequest)
		                                                  .post ()
		                                                  .getResponseByClass (OpenSearchResponse.class);
		if (response.getCode () != 200) {
			throw new AwsOpensearchException (response.getMessage ());
		}
		return response.getData ();
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
		Response<Map<String, Object>> response = HttpClient.builder ()
		                                                   .url (openSearchDomain + OpenSearchEndpoint._ALL.getPath ())
		                                                   .addHeader (HttpHeaders.AUTHORIZATION, getAuthorization ())
		                                                   .get ()
		                                                   .getResponseByMap ();
		if (response.getCode () != 200) {
			throw new AwsOpensearchException (response.getMessage ());
		}

		Map<String, List<String>> resultMap = new HashMap<> ();
		response.getData ().forEach ((k, v) -> extractIndexName (resultMap, k, indexName));
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
