package com.oriole.wisepen.common.core.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ModelType {
	STANDARD_MODEL(1,"STANDARD_MODEL",1),
	ADVANCED_MODEL(2,"ADVANCED_MODEL",10),
	UNKNOWN_MODEL(3,"UNKNOWN_MODEL",1);

	@EnumValue
	@JsonValue
	private final int code;
	private final String desc;
	private final int ratio;
	public static ModelType getByCode(Integer code) {
		if (code == null) {return null;}
		return Arrays.stream(values())
				.filter(t -> t.getCode() == code)
				.findFirst()
				.orElse(null);
	}
}
