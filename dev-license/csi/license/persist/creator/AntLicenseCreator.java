package csi.license.persist.creator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import csi.license.LicenseConstants;
import csi.license.persist.generator.HashLicenseGenerator;
import csi.license.persist.persistence.AbstractLicensePersistence;
import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;

//This is a class that can be called via the 'java' task in ANT to generate
//a Centrifuge Server license.  This class should not be distributed externally.
public class AntLicenseCreator {
   public static final DateTimeFormatter JAVA_UTIL_DATE_TOSTRING_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss z yyyy");

    /**
     * @param args
     * @throws IOException
     */
   public static void main(final String[] args) throws IOException {
      AbstractLicensePersistence license = null;

      switch (args.length) {
         case 8:  //LicensePersistenceV1
             // In the ANT task, the arguments are defined as follows.
             // <arg value="${license.customer}"/>
             // <arg value="${license.user_count}"/>
             // <arg value="${license.expiring}"/>
             // <arg value="${license.internal}"/>
             // <arg value="${license.expiration_date}"/>
             // <arg value="${license.ver_major}"/>
             // <arg value="${license.ver_minor}"/>
             // <arg value="${license.label}"/

            try {
               license =
                  new LicensePersistenceV1(args[0],  //customer
                                           Integer.parseInt(args[5]),  //versionMajor
                                           Integer.parseInt(args[6]),  //versionMinor
                                           Integer.parseInt(args[7]),  //versionLabel
                                           Integer.parseInt(args[1]),  //userCount
                                           Boolean.parseBoolean(args[3]),  //internal
                                           Boolean.parseBoolean(args[2]),  //expiring
                                           false,  //nodeLock
                                           DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).parse(args[4]));  //expirationDate
               HashLicenseGenerator licenseGenerator = new HashLicenseGenerator();
               String key = licenseGenerator.createLicenseFile(license);

               System.out.println(key);
               System.out.println(license);

               try (FileWriter fileWriter = new FileWriter(LicenseConstants.LICENSE_FILENAME);
                    BufferedWriter writer = new BufferedWriter(fileWriter)) {
                   writer.write(key);
                   writer.flush();
                } catch (IOException ioe) {
                   System.out.println(ioe.getMessage());
                }
            } catch (ParseException pe) {
               System.out.println(pe.getMessage());
            }
            break;
         case 10:  //LicensePersistenceV2
            // In the ANT task, the arguments are defined as follows.
            // <arg value="${license.customer}"/>
            // <arg value="${license.user_count}"/>
            // <arg value="${license.expiring}"/>
            // <arg value="${license.internal}"/>
            // <arg value="${license.expiration_date}"/>
            // <arg value="${license.ver_major}"/>
            // <arg value="${license.ver_minor}"/>
            // <arg value="${license.label}"/
            // <arg value="${license.concurrent}"/
            // <arg value="${license.start_date}"/

            license =
               new LicensePersistenceV2(args[0],  //customer
                                        Integer.parseInt(args[5]),  //versionMajor
                                        Integer.parseInt(args[6]),  //versionMinor
                                        Integer.parseInt(args[7]),  //versionLabel
                                        Integer.parseInt(args[1]),  //userCount
                                        Boolean.parseBoolean(args[3]),  //internal
                                        Boolean.parseBoolean(args[2]),  //expiring
                                        false,  //nodeLock
                                        Boolean.parseBoolean(args[8]),
                                        ZonedDateTime.parse(args[9], JAVA_UTIL_DATE_TOSTRING_FORMATTER),  //startDateTime
                                        ZonedDateTime.parse(args[4], JAVA_UTIL_DATE_TOSTRING_FORMATTER));  //expirationDateTime
            HashLicenseGenerator licenseGenerator = new HashLicenseGenerator();
            String key = licenseGenerator.createLicenseFile(license);

            System.out.println(key);
            System.out.println(license);

            try (FileWriter fileWriter = new FileWriter(LicenseConstants.LICENSE_FILENAME);
                 BufferedWriter writer = new BufferedWriter(fileWriter)) {
               writer.write(key);
               writer.flush();
            } catch (IOException ioe) {
               System.out.println(ioe.getMessage());
            }
            break;
         default:
            System.out.println("Error: Incorrect number of arguments.");
            System.exit(0);
      }
   }
}
