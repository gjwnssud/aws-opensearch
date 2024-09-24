package com.hzn.awsopensearch.controller.index;

import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.dto.index.AliasRequest;
import com.hzn.awsopensearch.dto.index.IndexRequest;
import com.hzn.awsopensearch.service.index.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Tag (name = "OpenSearch 인덱스 엔드 포인트")
@RestController
@RequestMapping ("/api/index")
@RequiredArgsConstructor
public class IndexController {
	private final IndexService indexService;

	@Operation (summary = "인덱스 생성")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequest.class)))
	@PutMapping
	public ResponseEntity<Response<Map<String, Object>>> createIndex (@Valid IndexRequest indexRequest) throws Exception {
		return ResponseEntity.ok (indexService.createIndex (indexRequest));
	}

	@Operation (summary = "인덱스 삭제")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequest.class)))
	@DeleteMapping
	public ResponseEntity<Response<Map<String, Object>>> deleteIndex (@Valid IndexRequest indexRequest) {
		return ResponseEntity.ok (indexService.deleteIndex (indexRequest));
	}

	@Operation (summary = "별칭 지정")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = AliasRequest.class)))
	@PostMapping ("/alias")
	public ResponseEntity<Response<Map<String, Object>>> setAlias (@Valid AliasRequest aliasRequest) {
		return ResponseEntity.ok (indexService.setAlias (aliasRequest));
	}

	@Operation (summary = "벌크 인덱싱")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequest.class)))
	@PostMapping ("/bulk")
	public ResponseEntity<Response<String>> bulk (@Valid IndexRequest indexRequest) {
		return ResponseEntity.ok (indexService.bulkIndexing (indexRequest));
	}

	@Operation (summary = "부분 인덱싱")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequest.class)))
	@PostMapping ("/upsert")
	public ResponseEntity<Response<String>> upsert (@Valid IndexRequest indexRequest) {
		return ResponseEntity.ok (indexService.upsertIndexing (indexRequest));
	}
}

