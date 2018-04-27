package jetbrains.buildServer.iaa.utils;

import java.util.Collections;
import java.util.Date;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.responsibility.BuildProblemResponsibilityEntry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.audit.AuditLogBuilder;
import jetbrains.buildServer.serverSide.audit.AuditLogProvider;
import jetbrains.buildServer.serverSide.impl.problems.BuildProblemImpl;
import jetbrains.buildServer.users.User;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

@Test
public class InvestigationsManagerTest extends BaseTestCase {

  private InvestigationsManager investigationsManager;
  private SProject project;
  private SProject project2;

  private BuildProblemImpl buildProblem;
  private BuildProblemResponsibilityEntry buildResponsibilityEntry1;


  private STestRun sTestRun;
  private STest sTest;
  private TestNameResponsibilityEntry testResponsibilityEntry1;
  private SBuild sBuild;
  private User user;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    project = Mockito.mock(SProject.class);
    project2 = Mockito.mock(SProject.class);
    sBuild = Mockito.mock(SBuild.class);
    user = Mockito.mock(User.class);
    SProject parentProject = Mockito.mock(SProject.class);
    when(project.getParentProject()).thenReturn(parentProject);
    when(project.getProjectId()).thenReturn("Project ID");
    when(project2.getProjectId()).thenReturn("Project ID 2");
    when(parentProject.getProjectId()).thenReturn("Parent Project ID");
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    buildProblem = Mockito.mock(BuildProblemImpl.class);
    buildResponsibilityEntry1 = Mockito.mock(BuildProblemResponsibilityEntry.class);
    when(buildResponsibilityEntry1.getTimestamp()).thenReturn(new Date(1000000));
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.NONE);
    when(buildProblem.getAllResponsibilities()).thenReturn(Collections.singletonList(buildResponsibilityEntry1));
    when(buildProblem.getProjectId()).thenReturn("123");

    sTestRun = Mockito.mock(STestRun.class);
    sTest = Mockito.mock(STest.class);
    testResponsibilityEntry1 = Mockito.mock(TestNameResponsibilityEntry.class);
    final AuditLogProvider auditLogProvider = Mockito.mock(AuditLogProvider.class);
    final AuditLogBuilder auditLogBuilder = Mockito.mock(AuditLogBuilder.class);
    when(auditLogProvider.getBuilder()).thenReturn(auditLogBuilder);
    when(testResponsibilityEntry1.getTimestamp()).thenReturn(new Date(1000000));
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.NONE);
    when(sTestRun.getTest()).thenReturn(sTest);
    when(sTest.getAllResponsibilities()).thenReturn(Collections.singletonList(testResponsibilityEntry1));
    when(sTest.getProjectId()).thenReturn("123");

    investigationsManager = new InvestigationsManager(auditLogProvider);
  }

  public void Test_BuildIsUnderInvestigationInSameProject() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.TAKEN);
    when(buildResponsibilityEntry1.getProject()).thenReturn(project);

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, buildProblem)).isTrue();
  }

  public void Test_BuildProblemIsUnderInvestigationParentProject() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.TAKEN);
    SProject parentProject = project.getParentProject();
    when(buildResponsibilityEntry1.getProject()).thenReturn(parentProject);

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, buildProblem)).isTrue();
  }

  public void Test_BuildProblemIsUnderInvestigationOtherProject() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.TAKEN);
    when(buildResponsibilityEntry1.getProject()).thenReturn(project2);

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, buildProblem)).isFalse();
  }

  public void Test_BuildProblemAlreadyFixed() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(buildResponsibilityEntry1.getProject()).thenReturn(project);
    when(buildResponsibilityEntry1.getTimestamp()).thenReturn(new Date(3000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, buildProblem)).isTrue();
  }

  public void Test_BuildProblemBeforeWasFixed() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(buildResponsibilityEntry1.getProject()).thenReturn(project);
    when(buildResponsibilityEntry1.getTimestamp()).thenReturn(new Date(1000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, buildProblem)).isFalse();
  }

  public void Test_BuildProblemNotUnderInvestigation() {
    when(buildProblem.getAllResponsibilities()).thenReturn(Collections.emptyList());

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, buildProblem)).isFalse();
  }

  public void Test_TestIsUnderInvestigationParentProject() {
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.TAKEN);
    SProject parentProject = project.getParentProject();
    when(testResponsibilityEntry1.getProject()).thenReturn(parentProject);

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, sTest)).isTrue();
  }

  public void Test_TestIsUnderInvestigationOtherProject() {
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.TAKEN);
    when(testResponsibilityEntry1.getProject()).thenReturn(project2);

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, sTest)).isFalse();
  }

  public void Test_TestAlreadyFixed() {
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(testResponsibilityEntry1.getProject()).thenReturn(project);
    when(testResponsibilityEntry1.getTimestamp()).thenReturn(new Date(3000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, sTest)).isTrue();
  }

  public void Test_TestBeforeWasAlreadyFixed() {
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(testResponsibilityEntry1.getProject()).thenReturn(project);
    when(testResponsibilityEntry1.getTimestamp()).thenReturn(new Date(1000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, sTest)).isFalse();
  }

  public void Test_TestNotUnderInvestigation() {
    STest sTest = Mockito.mock(STest.class);
    when(sTestRun.getTest()).thenReturn(sTest);
    when(sTest.getAllResponsibilities()).thenReturn(Collections.emptyList());

    Assertions.assertThat(investigationsManager.checkUnderInvestigation(project, sBuild, sTest)).isFalse();
  }

  public void Test_BuildProblemFindPreviousResponsible_FixedBeforeQueued() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(buildResponsibilityEntry1.getResponsibleUser()).thenReturn(user);
    when(buildResponsibilityEntry1.getProject()).thenReturn(project);
    when(buildResponsibilityEntry1.getTimestamp()).thenReturn(new Date(2000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(3000000));

    Assertions.assertThat(investigationsManager.findPreviousResponsible(project, sBuild, buildProblem)).isEqualTo(user);
  }

  public void Test_BuildProblemFindPreviousResponsible_FixedAfterQueued() {
    when(buildResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(buildResponsibilityEntry1.getProject()).thenReturn(project);
    when(buildResponsibilityEntry1.getTimestamp()).thenReturn(new Date(3000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    Assertions.assertThat(investigationsManager.findPreviousResponsible(project, sBuild, buildProblem)).isNull();
  }

  public void Test_TestProblemFindPreviousResponsible_FixedBeforeQueued() {
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(testResponsibilityEntry1.getResponsibleUser()).thenReturn(user);
    when(testResponsibilityEntry1.getProject()).thenReturn(project);
    when(testResponsibilityEntry1.getTimestamp()).thenReturn(new Date(2000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(3000000));

    Assertions.assertThat(investigationsManager.findPreviousResponsible(project, sBuild, sTest)).isEqualTo(user);
  }

  public void Test_TestProblemFindPreviousResponsible_FixedAfterQueued() {
    when(testResponsibilityEntry1.getState()).thenReturn(ResponsibilityEntry.State.FIXED);
    when(testResponsibilityEntry1.getProject()).thenReturn(project);
    when(testResponsibilityEntry1.getTimestamp()).thenReturn(new Date(3000000));
    when(sBuild.getQueuedDate()).thenReturn(new Date(2000000));

    Assertions.assertThat(investigationsManager.findPreviousResponsible(project, sBuild, sTest)).isNull();
  }
}