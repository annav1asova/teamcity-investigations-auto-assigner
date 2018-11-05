/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.iaa;

import java.util.Map;
import jetbrains.buildServer.iaa.common.Constants;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

public class AutoAssignerBuildFeature extends BuildFeature {

  private final String myEditUrl;

  @Autowired
  public AutoAssignerBuildFeature(
    @NotNull final PluginDescriptor descriptor) {
    myEditUrl = descriptor.getPluginResourcesPath("autoAssignerBuildFeature.jsp");
  }

  @NotNull
  @Override
  public String getType() {
    return Constants.BUILD_FEATURE_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return Constants.BUILD_FEATURE_DISPLAY_NAME;
  }

  @Nullable
  @Override
  public String getEditParametersUrl() {
    return myEditUrl;
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> params) {
    final String userName = params.get(Constants.DEFAULT_RESPONSIBLE);
    return "Default responsible is \"" + userName + "\"";
  }

  @Override
  public boolean isMultipleFeaturesPerBuildTypeAllowed() {
    return false;
  }
}
