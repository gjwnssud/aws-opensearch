package com.hzn.awsopensearch.controller.index;

import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.dto.index.AliasRequest;
import com.hzn.awsopensearch.dto.index.IndexRequest;
import com.hzn.awsopensearch.enums.Status;
import com.hzn.awsopensearch.service.index.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public ResponseEntity<Response<?>> createIndex (@Valid @RequestBody IndexRequest indexRequest) throws IOException {
		indexService.createIndex (indexRequest);
		return ResponseEntity.ok (Response.of (Status.OK));
	}

	@Operation (summary = "인덱스 삭제")
	@DeleteMapping
	public ResponseEntity<Response<?>> deleteIndex (@Valid @RequestBody IndexRequest indexRequest) throws IOException {
		indexService.deleteIndex (indexRequest);
		return ResponseEntity.ok (Response.of (Status.OK));
	}

	@Operation (summary = "별칭 지정")
	@PostMapping ("/alias")
	public ResponseEntity<Response<?>> setAlias (@Valid @RequestBody AliasRequest aliasRequest) throws IOException {
		indexService.setAlias (aliasRequest);
		return ResponseEntity.ok (Response.of (Status.OK));
	}
}

