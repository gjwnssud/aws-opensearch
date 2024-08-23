package com.hzn.awsopensearch.service.index;

import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.dto.index.AliasRequest;
import com.hzn.awsopensearch.dto.index.IndexRequest;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequest;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequest.Action;
import com.hzn.awsopensearch.dto.opensearch.OpenSearchAliasRequest.Action.Add;
import com.hzn.awsopensearch.enums.IndexType;
import com.hzn.awsopensearch.enums.OpenSearchEndpoint;
import com.hzn.awsopensearch.exception.AwsOpensearchException;
import com.hzn.awsopensearch.util.HttpClient;
import com.hzn.awsopensearch.util.HttpClient.Headers;
import com.hzn.awsopensearch.util.RetryHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Service
public class IndexService {
	@Value ("${cloud.aws.opensearch.domain}")
	private String openSearchDomain;
	@Value ("${cloud.aws.opensearch.autocomplete-index.name}")
	private String openSearchAutocompleteIndexName;
	@Value ("${cloud.aws.opensearch.search-index.name}")
	private String openSearchIndexName;
	@Value ("${cloud.aws.opensearch.master.id}")
	private String masterId;
	@Value ("${cloud.aws.opensearch.master.password}")
	private String masterPassword;

	public void createIndex (IndexRequest indexRequest) throws IOException {
		IndexType indexType = indexRequest.getIndexType ();
		String indexSettings = getIndexSettings (indexType);
		RetryHandler.retry (() -> {
			String indexName = getIndexName (indexType);
			Response<?> response;
			try {
				response = HttpClient.request (openSearchDomain + "/" + indexName, HttpMethod.PUT.name (), Headers.builder ()
				                                                                                                  .contentType (MediaType.APPLICATION_JSON)
				                                                                                                  .customHeaders (Map.of (HttpHeaders.AUTHORIZATION,
				                                                                                                                          getAuthorization ()))
				                                                                                                  .build (), indexSettings);
			} catch (IOException e) {
				throw new RuntimeException (e);
			}
			if (response.getCode () != 200) {
				throw new AwsOpensearchException ("createIndex failed. responseBody = " + response.getMessage ());
			} else {
				RetryHandler.resetRetryCount ();
			}
		}, 3);
	}

	public void deleteIndex (IndexRequest indexRequest) throws IOException {
		HttpClient.request (openSearchDomain + "/" + getIndexName (indexRequest.getIndexType ()), HttpMethod.DELETE.name (),
		                    Headers.builder ().contentType (MediaType.APPLICATION_JSON).customHeaders (Map.of (HttpHeaders.AUTHORIZATION, getAuthorization ())).build ());
	}

	public void setAlias (AliasRequest aliasRequest) throws IOException {
		HttpClient.request (openSearchDomain + OpenSearchEndpoint._ALIASES.getPath (), HttpMethod.POST.name (),
		                    Headers.builder ().contentType (MediaType.APPLICATION_JSON).customHeaders (Map.of (HttpHeaders.AUTHORIZATION, getAuthorization ())).build (),
		                    OpenSearchAliasRequest.builder ()
		                                          .actions (List.of (Action.builder ()
		                                                                   .add (Add.builder ()
		                                                                            .index (getIndexName (aliasRequest.getIndexType ()))
		                                                                            .alias (aliasRequest.getAliasName ())
		                                                                            .build ())
		                                                                   .build ()))
		                                          .build ());
	}

	private String getIndexSettings (IndexType indexType) throws IOException {
		try (BufferedReader br = new BufferedReader (new InputStreamReader (new ClassPathResource (
				indexType.name ().equals ("autocomplete") ? "opensearch/autocompleteIndexSettings.json" : "opensearch/searchIndexSettings.json").getInputStream (),
		                                                                    StandardCharsets.UTF_8))) {
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

	private String getIndexName (IndexType indexType) {
		return indexType.name ().equals ("autocomplete") ? openSearchAutocompleteIndexName : openSearchIndexName;
	}
}
