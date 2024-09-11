package com.hzn.awsopensearch.controller.index;

import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.dto.index.AliasRequest;
import com.hzn.awsopensearch.dto.index.IndexRequest;
import com.hzn.awsopensearch.service.index.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
	@PutMapping
	public ResponseEntity<Response<String>> createIndex (@Valid @ParameterObject IndexRequest indexRequest) throws Exception {
		return ResponseEntity.ok (indexService.createIndex (indexRequest));
	}

	@Operation (summary = "인덱스 삭제")
	@DeleteMapping
	public ResponseEntity<Response<String>> deleteIndex (@Valid @ParameterObject IndexRequest indexRequest) throws IOException {
		return ResponseEntity.ok (indexService.deleteIndex (indexRequest));
	}

	@Operation (summary = "별칭 지정")
	@PostMapping ("/alias")
	public ResponseEntity<Response<String>> setAlias (@Valid @ParameterObject AliasRequest aliasRequest) throws IOException {
		return ResponseEntity.ok (indexService.setAlias (aliasRequest));
	}

	@Operation (summary = "벌크 인덱싱")
	@PostMapping ("/bulk")
	public ResponseEntity<Response<String>> bulk (@Valid @ParameterObject IndexRequest indexRequest) {
		return ResponseEntity.ok (indexService.bulkIndexing (indexRequest));
	}

	@Operation (summary = "부분 인덱싱")
	@PostMapping ("/upsert")
	public ResponseEntity<Response<String>> upsert (@Valid @ParameterObject IndexRequest indexRequest) {
		return ResponseEntity.ok (indexService.upsertIndexing (indexRequest));
	}
}

