package com.bukovina.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.bukovina.platform")
class ModuleArchitectureTests {

  @ArchTest
  static final ArchRule SHARED_MUST_NOT_DEPEND_ON_BUSINESS_AREAS =
      noClasses()
          .that()
          .resideInAPackage("..shared..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..accommodation..", "..tourism..", "..support..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule ACCOMMODATION_MUST_NOT_DEPEND_ON_TOURISM =
      noClasses()
          .that()
          .resideInAPackage("..accommodation..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..tourism..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule TOURISM_MUST_NOT_DEPEND_ON_ACCOMMODATION =
      noClasses()
          .that()
          .resideInAPackage("..tourism..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule CONTROLLERS_MUST_NOT_DEPEND_ON_DAOS =
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..dao..");

  @ArchTest
  static final ArchRule GUESTHOUSE_DAO_MUST_REMAIN_INSIDE_GUESTHOUSE_MODULE =
      noClasses()
          .that()
          .resideOutsideOfPackage("..accommodation.guesthouse..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation.guesthouse.dao..");

  @ArchTest
  static final ArchRule TOP_LEVEL_PACKAGES_MUST_BE_FREE_OF_CYCLES =
      slices().matching("com.bukovina.platform.(*)..").should().beFreeOfCycles();
}
