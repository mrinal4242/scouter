/*
 *  Copyright 2015 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package scouter.org.objectweb.asm.tree;

import scouter.org.objectweb.asm.ModuleVisitor;
import scouter.org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * A node that represents an exported package with its name and the module that can access to it.
 * 
 * @author Remi Forax
 */
public class ModuleExportNode {
    /**
     * The package name.
     */
    public String packaze;
    
    /**
     * The access flags (see {@link Opcodes}).
     * Valid values are {@code ACC_SYNTHETIC} and {@code ACC_MANDATED}.
     */
    public int access;

    /**
     * A list of modules that can access to this exported package.
     * May be <tt>null</tt>.
     */
    public List<String> modules;

    /**
     * Constructs a new {@link ModuleExportNode}.
     * 
     * @param packaze
     *            the parameter's name.
     * @param modules
     *            a list of modules that can access to this exported package.
     */
    public ModuleExportNode(final String packaze, final int access, final List<String> modules) {
        this.packaze = packaze;
        this.access = access;
        this.modules = modules;
    }

    /**
     * Makes the given module visitor visit this export declaration.
     * 
     * @param mv
     *            a module visitor.
     */
    public void accept(final ModuleVisitor mv) {
        mv.visitExport(packaze, access, (modules == null) ? null : modules.toArray(new String[0]));
    }
}