package com.hzn.awsopensearch.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 23.
 */
@Getter
@RequiredArgsConstructor
public enum OpenSearchEndpoint {
	_SEARCH ("/_search"), _COUNT ("/_count"), _ALIASES ("/_aliases"), _ALL ("/_all"), _BULK ("/_bulk"), _DOC ("/_doc");
	private final String path;
}
