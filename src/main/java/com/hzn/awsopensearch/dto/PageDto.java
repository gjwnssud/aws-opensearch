package com.hzn.awsopensearch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 10.
 */
@Getter
@Setter
@SuperBuilder
public class PageDto {
	private int pageNumber;
	private int pageSize;

	public int getOffset () {
		return Math.max ((pageNumber - 1) * pageSize, 0);
	}
}
