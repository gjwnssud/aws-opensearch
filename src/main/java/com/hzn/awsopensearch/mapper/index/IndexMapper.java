package com.hzn.awsopensearch.mapper.index;

import com.hzn.awsopensearch.vo.index.CmtyNttInfo;
import com.hzn.awsopensearch.vo.index.CmtyNttRequest;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Mapper
public interface IndexMapper {
	List<CmtyNttInfo> getCmtyNttInfoList (CmtyNttRequest cmtyNttRequest);

	List<Integer> getIndexableNttSnList (CmtyNttRequest cmtyNttRequest);
}
