/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.api;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An enumeration of login states the client is in.
 */
@Getter
public enum LoginState
{
	/**
	 * Unknown login state.
	 */
	UNKNOWN(-1),

	WELCOME(0),
	// 1
	INPUT_CREDENTIALS(2),
	INVALID_CREDENTIALS(3),
	INPUT_AUTHENTICATOR(4),

	SET_DATE_OF_BIRTH(7),

	ACCOUNT_BLOCKED(14),

	FORCE_DISCONNECT(24),

	;

	private static final Map<Integer, LoginState> stateValueMap =
		Arrays.stream(LoginState.values())
			.collect(Collectors.toMap(gs -> gs.state, Function.identity()));

	/**
	 * The raw state value.
	 */
	private final int state;

	LoginState(int state)
	{
		this.state = state;
	}

	/**
	 * Utility method that maps the rank value to its respective
	 * {@link LoginState} value.
	 *
	 * @param state the raw state value
	 * @return the gamestate
	 */
	public static LoginState of(int state)
	{
		return stateValueMap.getOrDefault(state, UNKNOWN);
	}
}
