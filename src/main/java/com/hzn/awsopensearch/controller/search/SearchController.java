package com.hzn.awsopensearch.controller.search;

import com.hzn.awsopensearch.dto.ResponseDto;
import com.hzn.awsopensearch.dto.search.AutocompleteRequestDto;
import com.hzn.awsopensearch.dto.search.SearchRequestDto;
import com.hzn.awsopensearch.enums.Status;
import com.hzn.awsopensearch.service.search.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 24.
 */
@Tag (name = "OpenSearch 검색 엔드 포인트")
@RestController
@RequestMapping ("/api/search")
@RequiredArgsConstructor
public class SearchController {
	private final SearchService searchService;

	@Operation (summary = "검색어 자동 완성")
	@GetMapping ("/autocomplete")
	public ResponseEntity<ResponseDto<List<String>>> autocomplete (@Valid @ParameterObject AutocompleteRequestDto autocompleteRequestDto) {
		return ResponseEntity.ok (ResponseDto.of (Status.OK, searchService.autocomplete (autocompleteRequestDto)));
	}

	@Operation (summary = "게시글 검색")
	@GetMapping
	public ResponseEntity<ResponseDto<List<Map<String, Object>>>> nttSearch (@Valid @ParameterObject SearchRequestDto searchRequestDto) {
		return ResponseEntity.ok (ResponseDto.of (Status.OK, searchService.nttSearch (searchRequestDto)));
	}
}
