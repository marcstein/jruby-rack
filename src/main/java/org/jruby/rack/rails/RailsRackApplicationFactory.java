/*
 ***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2007 Sun Microsystems, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.jruby.rack.rails;

import javax.servlet.ServletContext;
import org.jruby.Ruby;
import org.jruby.rack.DefaultRackApplicationFactory;
import org.jruby.rack.RackInitializationException;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author nicksieger
 */
public class RailsRackApplicationFactory extends DefaultRackApplicationFactory {
    private String publicRoot;
    private String railsEnv;
    private String railsRoot;

    @Override
    public void init(ServletContext servletContext) {
        railsRoot = servletContext.getInitParameter("rails.root");
        if (railsRoot == null) {
            railsRoot = "/WEB-INF";
        }
        railsRoot = servletContext.getRealPath(railsRoot);
        railsEnv = servletContext.getInitParameter("rails.env");
        if (railsEnv == null) {
            railsEnv = "production";
        }
        publicRoot = servletContext.getInitParameter("public.root");
        if (publicRoot == null) {
            publicRoot = "/WEB-INF/public";
        }
        publicRoot = servletContext.getRealPath(publicRoot);
    }

    @Override
    public Ruby newRuntime() throws RackInitializationException {
        Ruby runtime = super.newRuntime();
        runtime.evalScriptlet("ENV").callMethod(runtime.getCurrentContext(), "[]=",
                new IRubyObject[] {runtime.newString("RAILS_ENV"), runtime.newString(railsEnv)});
        runtime.evalScriptlet("ENV").callMethod(runtime.getCurrentContext(), "[]=",
                new IRubyObject[] {runtime.newString("RAILS_ROOT"), runtime.newString(railsRoot)});
        return runtime;
    }

    @Override
    public IRubyObject createApplicationObject(Ruby runtime) {
        return createRackServletWrapper(runtime,
                "require 'rack/adapter/rails_bootstrap'\n" +
                "app = Rack::Adapter::Rails.new\n" +
                "file = Rack::File.new(%{" + publicRoot + "})\n" +
                "app = Rack::Cascade.new([file, app])\n" +
                "run app\n");
    }

    public String getRailsEnv() {
        return railsEnv;
    }

    public String getRailsRoot() {
        return railsRoot;
    }

    public String getPublicRoot() {
        return publicRoot;
    }
}
