package com.hzn.awsopensearch.controller.index;

import com.hzn.awsopensearch.dto.ResponseDto;
import com.hzn.awsopensearch.dto.index.AliasRequestRequestDto;
import com.hzn.awsopensearch.dto.index.IndexRequestDto;
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
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequestDto.class)))
	@PutMapping
	public ResponseEntity<ResponseDto<Map<String, Object>>> createIndex (@Valid IndexRequestDto indexRequestDto) throws Exception {
		return ResponseEntity.ok (indexService.createIndex (indexRequestDto));
	}

	@Operation (summary = "인덱스 삭제")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequestDto.class)))
	@DeleteMapping
	public ResponseEntity<ResponseDto<Map<String, Object>>> deleteIndex (@Valid IndexRequestDto indexRequestDto) {
		return ResponseEntity.ok (indexService.deleteIndex (indexRequestDto));
	}

	@Operation (summary = "별칭 지정")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = AliasRequestRequestDto.class)))
	@PostMapping ("/alias")
	public ResponseEntity<ResponseDto<Map<String, Object>>> setAlias (@Valid AliasRequestRequestDto aliasRequestDto) {
		return ResponseEntity.ok (indexService.setAlias (aliasRequestDto));
	}

	@Operation (summary = "벌크 인덱싱")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequestDto.class)))
	@PostMapping ("/bulk")
	public ResponseEntity<ResponseDto<String>> bulk (@Valid IndexRequestDto indexRequestDto) {
		return ResponseEntity.ok (indexService.bulkIndexing (indexRequestDto));
	}

	@Operation (summary = "부분 인덱싱")
	@RequestBody (content = @Content (mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema (implementation = IndexRequestDto.class)))
	@PostMapping ("/upsert")
	public ResponseEntity<ResponseDto<String>> upsert (@Valid IndexRequestDto indexRequestDto) {
		return ResponseEntity.ok (indexService.upsertIndexing (indexRequestDto));
	}
}

