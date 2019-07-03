/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lucas.component.common.extension.ext1.impl;

import org.lucas.component.common.extension.ext1.SimpleExt;
import org.lucas.component.common.extension.ExtURL;

public class SimpleExtImpl3 implements SimpleExt {
    public String echo(ExtURL url, String s) {
        return "Ext1Impl3-echo";
    }

    public String yell(ExtURL url, String s) {
        return "Ext1Impl3-yell";
    }

    public String bang(ExtURL url, int i) {
        return "bang3";
    }

}