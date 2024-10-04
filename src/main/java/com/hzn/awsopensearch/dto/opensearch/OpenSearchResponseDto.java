package com.hzn.awsopensearch.dto.opensearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hzn.awsopensearch.dto.index.CmtyNttRequestDto;
import com.hzn.awsopensearch.dto.index.CmtyNttRequestDto.CmtyNttRequestDtoBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 23.
 */
@Getter
@Setter
@ToString
@JsonInclude (JsonInclude.Include.NON_NULL)
public class OpenSearchResponseDto {
	private long took;
	private boolean timed_out;
	private Shards _shards;
	private Hits hits;
	private int count;

	private Error error;
	private int status;

	private Aggregations aggregations;

	@Getter
	@Setter
	@ToString
	public static class Shards {
		private long total;
		private long successful;
		private long skipped;
		private long failed;
	}

	@Getter
	@Setter
	@ToString
	public static class Hits {
		private Total total;
		private double max_score;
		private List<SubHits> hits;

		@Getter
		@Setter
		@ToString
		public static class Total {
			private long value;
			private String relation;
		}

		@Getter
		@Setter
		@ToString
		@JsonInclude (JsonInclude.Include.NON_NULL)
		public static class SubHits {
			private String _index;
			private String _id;
			private double _score;
			private Map<String, Object> _source;
			private List<Long> sort;
		}
	}

	@Getter
	@Setter
	@ToString
	public static class Error {
		private String type;
		private String reason;
		private int line;
		private int col;
		private List<Map<String, Object>> root_cause;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public static class Aggregations {
		private Values cmtyNttMaxSysRegistDt;
		private Values cmtyNttMaxSysUpdtDt;
		private Values cmtyNttAnswersMaxSysRegistDt;
		private Values cmtyNttAnswersMaxSysUpdtDt;
		private Values nttBlckgInfoMaxSysRegistDt;
		private Values nttBlckgInfoMaxSysUpdtDt;
		private Values cmtyFrendBlckgInfoMaxSysRegistDt;
		private Values cmtyFrendBlckgInfoMaxSysUpdtDt;
		private Values cmtyFrendBlckgInfo2MaxSysRegistDt;
		private Values cmtyFrendBlckgInfo2MaxSysUpdtDt;

		public static Aggregations merge (Aggregations... others) {
			AggregationsBuilder aggregationsBuilder = Aggregations.builder ();
			Arrays.stream (others).forEach (other -> {
				if (other.cmtyNttMaxSysRegistDt != null) {
					aggregationsBuilder.cmtyNttMaxSysRegistDt (other.cmtyNttMaxSysRegistDt);
				}
				if (other.cmtyNttMaxSysUpdtDt != null) {
					aggregationsBuilder.cmtyNttMaxSysUpdtDt (other.cmtyNttMaxSysUpdtDt);
				}
				if (other.cmtyNttAnswersMaxSysRegistDt != null) {
					aggregationsBuilder.cmtyNttAnswersMaxSysRegistDt (other.cmtyNttAnswersMaxSysRegistDt);
				}
				if (other.cmtyNttAnswersMaxSysUpdtDt != null) {
					aggregationsBuilder.cmtyNttAnswersMaxSysUpdtDt (other.cmtyNttAnswersMaxSysUpdtDt);
				}
				if (other.nttBlckgInfoMaxSysRegistDt != null) {
					aggregationsBuilder.nttBlckgInfoMaxSysRegistDt (other.nttBlckgInfoMaxSysRegistDt);
				}
				if (other.nttBlckgInfoMaxSysUpdtDt != null) {
					aggregationsBuilder.nttBlckgInfoMaxSysUpdtDt (other.nttBlckgInfoMaxSysUpdtDt);
				}
				if (other.cmtyFrendBlckgInfoMaxSysRegistDt != null) {
					aggregationsBuilder.cmtyFrendBlckgInfoMaxSysRegistDt (other.cmtyFrendBlckgInfoMaxSysRegistDt);
				}
				if (other.cmtyFrendBlckgInfoMaxSysUpdtDt != null) {
					aggregationsBuilder.cmtyFrendBlckgInfoMaxSysUpdtDt (other.cmtyFrendBlckgInfoMaxSysUpdtDt);
				}
				if (other.cmtyFrendBlckgInfo2MaxSysRegistDt != null) {
					aggregationsBuilder.cmtyFrendBlckgInfo2MaxSysRegistDt (other.cmtyFrendBlckgInfo2MaxSysRegistDt);
				}
				if (other.cmtyFrendBlckgInfo2MaxSysUpdtDt != null) {
					aggregationsBuilder.cmtyFrendBlckgInfo2MaxSysUpdtDt (other.cmtyFrendBlckgInfo2MaxSysUpdtDt);
				}
			});
			return aggregationsBuilder.build ();
		}

		public CmtyNttRequestDto toCmtyNttRequest () {
			CmtyNttRequestDtoBuilder<?, ?> searchRequestBuilder = CmtyNttRequestDto.builder ();
			if (!ObjectUtils.isEmpty (cmtyNttMaxSysRegistDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyNttMaxSysRegistDt (LocalDateTime.parse (cmtyNttMaxSysRegistDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyNttMaxSysUpdtDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyNttMaxSysUpdtDt (LocalDateTime.parse (cmtyNttMaxSysUpdtDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyNttAnswersMaxSysRegistDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyNttAnswersMaxSysRegistDt (
						LocalDateTime.parse (cmtyNttAnswersMaxSysRegistDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyNttAnswersMaxSysUpdtDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyNttAnswersMaxSysUpdtDt (LocalDateTime.parse (cmtyNttAnswersMaxSysUpdtDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (nttBlckgInfoMaxSysRegistDt.getValue_as_string ())) {
				searchRequestBuilder.nttBlckgInfoMaxSysRegistDt (LocalDateTime.parse (nttBlckgInfoMaxSysRegistDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (nttBlckgInfoMaxSysUpdtDt.getValue_as_string ())) {
				searchRequestBuilder.nttBlckgInfoMaxSysUpdtDt (LocalDateTime.parse (nttBlckgInfoMaxSysUpdtDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyFrendBlckgInfoMaxSysRegistDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyFrendBlckgInfoMaxSysRegistDt (
						LocalDateTime.parse (cmtyFrendBlckgInfoMaxSysRegistDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyFrendBlckgInfoMaxSysUpdtDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyFrendBlckgInfoMaxSysUpdtDt (
						LocalDateTime.parse (cmtyFrendBlckgInfoMaxSysUpdtDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyFrendBlckgInfo2MaxSysRegistDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyFrendBlckgInfo2MaxSysRegistDt (
						LocalDateTime.parse (cmtyFrendBlckgInfo2MaxSysRegistDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			if (!ObjectUtils.isEmpty (cmtyFrendBlckgInfo2MaxSysUpdtDt.getValue_as_string ())) {
				searchRequestBuilder.cmtyFrendBlckgInfo2MaxSysUpdtDt (
						LocalDateTime.parse (cmtyFrendBlckgInfo2MaxSysUpdtDt.getValue_as_string (), DateTimeFormatter.ISO_DATE_TIME));
			}
			return searchRequestBuilder.build ();
		}

		@Setter
		@Getter
		@JsonInclude (JsonInclude.Include.NON_NULL)
		public static class Values {
			private Long value;
			private String value_as_string;
		}
	}
}
