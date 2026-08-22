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
          .resideInAPackage("com.bukovina.platform..dao..");

  @ArchTest
  static final ArchRule TOURISM_SERVICES_MUST_NOT_ACCESS_PERSISTENCE_OR_WEB_TYPES =
      noClasses()
          .that()
          .resideInAnyPackage("..tourism.activity.service..", "..tourism.startour.service..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework.jdbc..", "java.sql..", "org.springframework.web..");

  @ArchTest
  static final ArchRule TOURISM_DAOS_MUST_NOT_DEPEND_ON_DTOS =
      noClasses()
          .that()
          .resideInAnyPackage("..tourism.activity.dao..", "..tourism.startour.dao..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..tourism.activity.dto..", "..tourism.startour.dto..");

  @ArchTest
  static final ArchRule TOURISM_MODELS_MUST_NOT_DEPEND_ON_DTOS =
      noClasses()
          .that()
          .resideInAnyPackage("..tourism.activity.model..", "..tourism.startour.model..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..tourism.activity.dto..", "..tourism.startour.dto..");

  @ArchTest
  static final ArchRule GUESTHOUSE_DAO_MUST_REMAIN_INSIDE_GUESTHOUSE_MODULE =
      noClasses()
          .that()
          .resideOutsideOfPackage("..accommodation.guesthouse..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation.guesthouse.dao..");

  @ArchTest
  static final ArchRule ROOM_TYPE_DAO_MUST_REMAIN_INSIDE_ROOM_TYPE_MODULE =
      noClasses()
          .that()
          .resideOutsideOfPackage("..accommodation.roomtype..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation.roomtype.dao..");

  @ArchTest
  static final ArchRule AMENITY_DAO_MUST_REMAIN_INSIDE_AMENITY_MODULE =
      noClasses()
          .that()
          .resideOutsideOfPackage("..accommodation.amenity..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation.amenity.dao..");

  @ArchTest
  static final ArchRule PRICING_DAO_MUST_REMAIN_INSIDE_PRICING_MODULE =
      noClasses()
          .that()
          .resideOutsideOfPackage("..accommodation.pricing..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation.pricing.dao..");

  @ArchTest
  static final ArchRule BOOKING_DAO_MUST_REMAIN_INSIDE_BOOKING_MODULE =
      noClasses()
          .that()
          .resideOutsideOfPackage("..accommodation.booking..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..accommodation.booking.dao..");

  @ArchTest
  static final ArchRule TOP_LEVEL_PACKAGES_MUST_BE_FREE_OF_CYCLES =
      slices().matching("com.bukovina.platform.(*)..").should().beFreeOfCycles();
}
