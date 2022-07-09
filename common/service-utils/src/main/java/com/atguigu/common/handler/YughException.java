package com.atguigu.common.handler;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WangJin
 * @create 2022-06-16 16:07
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YughException extends RuntimeException{

    @ApiModelProperty(value = "状态码")
    private Integer code;

    private String msg;
}
