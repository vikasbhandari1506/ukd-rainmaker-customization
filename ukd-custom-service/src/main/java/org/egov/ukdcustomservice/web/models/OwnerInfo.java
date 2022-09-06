package org.egov.ukdcustomservice.web.models;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;

import org.egov.common.contract.request.Role;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * OwnerInfo
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-11T14:12:44.497+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OwnerInfo extends User  {

        @JsonProperty("isPrimaryOwner")
        private Boolean isPrimaryOwner;

        @JsonProperty("ownerShipPercentage")
        private Double ownerShipPercentage;

        @JsonProperty("ownerType")
        private String ownerType;

        @JsonProperty("institutionId")
        private String institutionId;


        public enum RelationshipEnum {
                FATHER("FATHER"),

                HUSBAND("HUSBAND");

                private String value;

                RelationshipEnum(String value) {
                        this.value = value;
                }

                @Override
                @JsonValue
                public String toString() {
                        return String.valueOf(value);
                }

                @JsonCreator
                public static RelationshipEnum fromValue(String text) {
                        for (RelationshipEnum b : RelationshipEnum.values()) {
                                if (String.valueOf(b.value).equals(text)) {
                                        return b;
                                }
                        }
                        return null;
                }
        }

        @JsonProperty("relationship")
        private RelationshipEnum relationship;

        /**
         * status of the owner
         */
        public enum OwnerStatus {

                ACTIVE ("ACTIVE"),

                INACTIVE ("INACTIVE"),

                INWORKFLOW ("INWORKFLOW"),

                CANCELLED ("CANCELLED"),

                REJECTED ("REJECTED");

                private String value;

                OwnerStatus(String value) {
                        this.value = value;
                }

                @Override
                @JsonValue
                public String toString() {
                        return String.valueOf(value);
                }

                @JsonCreator
                public static OwnerStatus fromValue(String text) {
                        for (OwnerStatus b : OwnerStatus.values()) {
                                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                                        return b;
                                }
                        }
                        return null;
                }
        }

        @JsonProperty("status")
        private OwnerStatus status;

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                if (!super.equals(o)) return false;

                User user = (User) o;

                return Objects.equals(this.getUuid(), user.getUuid()) &&
                        Objects.equals(this.getName(), user.getName()) &&
                        Objects.equals(this.getMobileNumber(), user.getMobileNumber());
        }

        @Override
        public int hashCode() {

                return super.hashCode();
        }
        
        /**
         * status of the Property
         */
        public enum Status {

                ACTIVE ("ACTIVE"),

                INACTIVE ("INACTIVE"),

                INWORKFLOW ("INWORKFLOW"),

                CANCELLED ("CANCELLED"),

                REJECTED ("REJECTED");

                private String value;

                Status(String value) {
                        this.value = value;
                }

                @Override
                @JsonValue
                public String toString() {
                        return String.valueOf(value);
                }

                @JsonCreator
                public static Status fromValue(String text) {
                        for (Status b : Status.values()) {
                                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                                        return b;
                                }
                        }
                        return null;
                }
        }

}

