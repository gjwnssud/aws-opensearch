package com.hzn.awsopensearch.dto.opensearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 23.
 */
@Getter
@Builder
public class OpenSearchAliasRequest {
	private List<Action> actions;

	@Getter
	@Builder
	@JsonInclude (JsonInclude.Include.NON_NULL)
	public static class Action {
		private Add add;
		private Remove remove;

		@Getter
		@Builder
		public static class Add {
			private String index;
			private String alias;
		}

		@Getter
		@Builder
		public static class Remove {
			private String index;
			private String alias;
		}
	}

}
