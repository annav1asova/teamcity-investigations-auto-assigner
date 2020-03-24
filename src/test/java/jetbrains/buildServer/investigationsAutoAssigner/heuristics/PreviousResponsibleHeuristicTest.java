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

package jetbrains.buildServer.investigationsAutoAssigner.heuristics;

import java.util.Collections;
import java.util.HashSet;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.investigationsAutoAssigner.common.HeuristicResult;
import jetbrains.buildServer.investigationsAutoAssigner.common.Responsibility;
import jetbrains.buildServer.investigationsAutoAssigner.processing.HeuristicContext;
import jetbrains.buildServer.investigationsAutoAssigner.utils.InvestigationsManager;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.problems.BuildProblem;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.users.UserSet;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Test
public class PreviousResponsibleHeuristicTest extends BaseTestCase {

  private PreviousResponsibleHeuristic myHeuristic;
  private InvestigationsManager myInvestigationsManager;
  private SBuild mySBuild;
  private SProject mySProject;
  private BuildProblem myBuildProblem;
  private User myUser;
  private STest mySTest;
  private STestRun mySTestRun;
  private HeuristicContext myBuildHeuristicContext;
  private HeuristicContext myTestHeuristicContext;
  private User myUser2;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myInvestigationsManager = Mockito.mock(InvestigationsManager.class);
    mySBuild = Mockito.mock(SBuild.class);
    final SBuildType sBuildType = Mockito.mock(jetbrains.buildServer.serverSide.SBuildType.class);
    mySProject = Mockito.mock(SProject.class);
    myBuildProblem = Mockito.mock(BuildProblem.class);
    final BuildProblemData buildProblemData = Mockito.mock(BuildProblemData.class);
    myUser = Mockito.mock(SUser.class);
    when(myUser.getId()).thenReturn(1L);
    myUser2 = Mockito.mock(SUser.class);
    when(myUser2.getId()).thenReturn(2L);
    mySTest = Mockito.mock(STest.class);
    when(myUser.getUsername()).thenReturn("testUser");
    when(myUser2.getUsername()).thenReturn("testUser 2");
    UserSet userSetMock = Mockito.mock(UserSet.class);
    when(userSetMock.getUsers()).thenReturn(new HashSet<>(Collections.singletonList(myUser)));
    when(mySBuild.getCommitters(any())).thenReturn(userSetMock);

    myHeuristic = new PreviousResponsibleHeuristic(myInvestigationsManager);
    when(myBuildProblem.getBuildProblemData()).thenReturn(buildProblemData);
    when(buildProblemData.getType()).thenReturn("Type");
    when(mySBuild.getFullName()).thenReturn("Full SBuild Name");
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, myBuildProblem)).thenReturn(myUser);
    when(mySBuild.getBuildType()).thenReturn(sBuildType);
    when(sBuildType.getProject()).thenReturn(mySProject);
    when(mySTest.getTestNameId()).thenReturn(12982318457L);
    when(mySTest.getProjectId()).thenReturn("2134124");
    mySTestRun = Mockito.mock(STestRun.class);
    when(mySTestRun.getTest()).thenReturn(mySTest);
    myBuildHeuristicContext = new HeuristicContext(mySBuild,
                                                   mySProject,
                                                   Collections.singletonList(myBuildProblem),
                                                   Collections.emptyList(),
                                                   Collections.emptySet());
    myTestHeuristicContext = new HeuristicContext(mySBuild,
                                                  mySProject,
                                                  Collections.emptyList(),
                                                  Collections.singletonList(mySTestRun),
                                                  Collections.emptySet());
  }

  public void TestBuildProblemInfo_ResponsibleFound() {
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, myBuildProblem)).thenReturn(myUser);

    HeuristicResult result = myHeuristic.findResponsibleUser(myBuildHeuristicContext);

    Assert.assertFalse(result.isEmpty());
    Responsibility responsibility = result.getResponsibility(myBuildProblem);
    assert responsibility != null;
    Assert.assertEquals(responsibility.getUser(), myUser);
  }

  public void Test_FoundResponsibleNotAmongCommiters() {
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, myBuildProblem)).thenReturn(myUser2);

    HeuristicResult result = myHeuristic.findResponsibleUser(myBuildHeuristicContext);
//
    Assert.assertTrue(result.isEmpty());
  }

  public void TestBuildProblemInfo_ResponsibleNotFound() {
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, myBuildProblem)).thenReturn(null);

    HeuristicResult result = myHeuristic.findResponsibleUser(myBuildHeuristicContext);
//
    Assert.assertTrue(result.isEmpty());
  }

  public void TestTestProblemInfo_ResponsibleFound() {
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, mySTest)).thenReturn(myUser);

    HeuristicResult result = myHeuristic.findResponsibleUser(myTestHeuristicContext);

    Assert.assertFalse(result.isEmpty());
    Assert.assertNotNull(result.getResponsibility(mySTestRun));
    Responsibility responsibility = result.getResponsibility(mySTestRun);
    assert responsibility != null;
    Assert.assertEquals(responsibility.getUser(), myUser);
  }

  public void TestTestProblemInfo_ResponsibleNotFound() {
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, mySTest)).thenReturn(null);

    HeuristicResult result = myHeuristic.findResponsibleUser(myTestHeuristicContext);

    Assert.assertTrue(result.isEmpty());
  }

  public void TestWhiteList() {
    when(myInvestigationsManager.findPreviousResponsible(mySProject, mySBuild, mySTest)).thenReturn(myUser);
    HeuristicContext testHC = new HeuristicContext(mySBuild,
                                                   mySProject,
                                                   Collections.singletonList(myBuildProblem),
                                                   Collections.emptyList(),
                                                   Collections.singleton(myUser.getUsername()));
    HeuristicContext buildProblemsHc = new HeuristicContext(mySBuild,
                                                            mySProject,
                                                            Collections.emptyList(),
                                                            Collections.singletonList(mySTestRun),
                                                            Collections.singleton(myUser.getUsername()));
    HeuristicResult result = myHeuristic.findResponsibleUser(testHC);
    Assert.assertTrue(result.isEmpty());

    result = myHeuristic.findResponsibleUser(buildProblemsHc);
    Assert.assertTrue(result.isEmpty());
  }
}