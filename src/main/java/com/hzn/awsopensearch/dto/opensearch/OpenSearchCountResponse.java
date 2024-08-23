package com.hzn.awsopensearch.dto.opensearch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 23.
 */
@Setter
@Getter
@ToString
public class OpenSearchCountResponse {
	private int count;
	private Shards _shards;

	@Getter
	@Setter
	@ToString
	public static class Shards {
		private long total;
		private long successful;
		private long skipped;
		private long failed;
	}
}
