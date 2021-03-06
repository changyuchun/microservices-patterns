/*
 * Copyright 2020 Wuyi Chen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ftgo.deliveryservice.api.model;

/**
 * The delivery info.
 * 
 * @author  Wuyi Chen
 * @date    05/16/2020
 * @version 1.0
 * @since   1.0
 */
public class DeliveryInfo {
	private long   id;
	private String state;

	public DeliveryInfo() {
	}

	public DeliveryInfo(long id, String state) {
		this.id    = id;
		this.state = state;
	}

	public long   getId()                { return id;          }
	public void   setId(long id)         { this.id = id;       }
	public String getState()             { return state;       }
	public void   setState(String state) { this.state = state; }
}
