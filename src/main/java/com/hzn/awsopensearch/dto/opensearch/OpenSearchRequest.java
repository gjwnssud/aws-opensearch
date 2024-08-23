package com.hzn.awsopensearch.dto.opensearch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>OpenSearch 검색용 객체</p>
 *
 * @author hzn
 * @date 2024. 8. 23.
 */
@Getter
@Builder
@JsonInclude (JsonInclude.Include.NON_NULL)
public class OpenSearchRequest {
	private int from;
	private int size;
	private Query query;
	private List<Map<String, SortProperties>> sort;
	@JsonIgnore
	private boolean autocomplete;
	@JsonIgnore
	private Type type;
	private Aggregations aggs;

	@Getter
	@Builder
	@JsonInclude (JsonInclude.Include.NON_NULL)
	public static class Query {
		private Bool bool;
		private MultiMatch multi_match;

		@Getter
		@Builder
		@JsonInclude (JsonInclude.Include.NON_NULL)
		public static class MultiMatch {
			private String query;
			private String type;
			private List<String> fields;
		}

		@Getter
		@Builder
		@JsonInclude (JsonInclude.Include.NON_NULL)
		public static class Bool {
			private List<Must> must;
			private List<Filter> filter;
			private List<MustNot> must_not;
			private List<Should> should;
			private Integer minimum_should_match;

			@Getter
			@Builder
			@JsonInclude (JsonInclude.Include.NON_NULL)
			public static class Must {
				private MultiMatch multi_match;
				private Map<String, Object> term;
				private Bool bool;
				private Map<String, RangeProperties> range;
				private Exists exists;

				@Getter
				@Builder
				public static class Exists {
					private String field;
				}
			}

			@Getter
			@Builder
			@JsonInclude (JsonInclude.Include.NON_NULL)
			public static class Filter {
				private Map<String, Object> term;
				private Map<String, Object> range;
			}

			@Getter
			@Builder
			@JsonInclude (JsonInclude.Include.NON_NULL)
			public static class MustNot {
				private Map<String, Object> term;
			}

			@Getter
			@Builder
			@JsonInclude (JsonInclude.Include.NON_NULL)
			public static class Should {
				private Map<String, Object> term;
				private Bool bool;
			}
		}
	}

	@Getter
	@Builder
	@JsonInclude (JsonInclude.Include.NON_NULL)
	public static class SortProperties {
		private String order;
		private String mode;
		private String missing;
		private String unmapped_type;
	}

	@Getter
	@Builder
	@JsonInclude (JsonInclude.Include.NON_NULL)
	public static class RangeProperties {
		private String lte;
		private String gte;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Type {
		_SEARCH ("/_search"), _COUNT ("/_count");
		private final String path;
	}

	@Getter
	@Builder
	@JsonInclude (JsonInclude.Include.NON_NULL)
	public static class Aggregations {
		private Map<String, Map<String, Object>> cmtyNttMaxSysRegistDt;
		private Map<String, Map<String, Object>> cmtyNttMaxSysUpdtDt;
		private Map<String, Map<String, Object>> cmtyNttAnswersMaxSysRegistDt;
		private Map<String, Map<String, Object>> cmtyNttAnswersMaxSysUpdtDt;
		private Map<String, Map<String, Object>> nttBlckgInfoMaxSysRegistDt;
		private Map<String, Map<String, Object>> nttBlckgInfoMaxSysUpdtDt;
		private Map<String, Map<String, Object>> cmtyFrendBlckgInfoMaxSysRegistDt;
		private Map<String, Map<String, Object>> cmtyFrendBlckgInfoMaxSysUpdtDt;
		private Map<String, Map<String, Object>> cmtyFrendBlckgInfo2MaxSysRegistDt;
		private Map<String, Map<String, Object>> cmtyFrendBlckgInfo2MaxSysUpdtDt;
	}
}
