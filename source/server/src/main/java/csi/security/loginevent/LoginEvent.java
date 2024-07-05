package csi.security.loginevent;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "login_events")
public class LoginEvent implements Serializable {
   private static final long serialVersionUID = 6849751226409059175L;

   @Id
   @GeneratedValue
   @Column(name = "id")
   private int id;

   @Column(name = "event_date_time", nullable = false)
   private Timestamp eventDateTime;

   @Column(name = "reason", length = 40, nullable = false)
   private EventReasons reason;

   @Column(name = "user_name", length = 40, nullable = false)
   private String userName;

   @Column(name = "known_user", nullable = false)
   private boolean knownUser;

   @Column(name = "active_users_at_event_time", nullable = false)
   private int activeUsersAtEventTime;

   @Column(name = "known_users_at_event_time", nullable = false)
   private int knownUsersAtEventTime;

   public LoginEvent() {
   }

   public LoginEvent(final EventReasons reason, final Timestamp eventDateTime,
                     final String userName, final boolean knownUser,
                     final int activeUsersAtEventTime, final int knownUsersAtEventTime) {
      this.reason = reason;
      this.eventDateTime = eventDateTime;
      this.userName = userName;
      this.knownUser = knownUser;
      this.activeUsersAtEventTime = activeUsersAtEventTime;
      this.knownUsersAtEventTime = knownUsersAtEventTime;
   }

   public LoginEvent(final int id, final EventReasons reason, final Timestamp eventDateTime,
                     final String userName, final boolean knownUser,
                     final int activeUsersAtEventTime, final int knownUsersAtEventTime) {
      this(reason, eventDateTime, userName, knownUser, activeUsersAtEventTime, knownUsersAtEventTime);
   }

   public int getId() {
      return id;
   }
   public EventReasons getReason() {
      return reason;
   }
   public Timestamp getEventDateTime() {
      return eventDateTime;
   }
   public String getUserName() {
      return userName;
   }
   public boolean isKnownUser() {
      return knownUser;
   }
   public int getActiveUsersAtEventTime() {
      return activeUsersAtEventTime;
   }
   public int getKnownUsersAtEventTime() {
      return knownUsersAtEventTime;
   }

   public void setId(final int id) {
      this.id = id;
   }
   public void setReason(final EventReasons reason) {
      this.reason = reason;
   }
   public void setEventDateTime(final Timestamp eventDateTime) {
      this.eventDateTime = eventDateTime;
   }
   public void setUserName(final String userName) {
      this.userName = userName;
   }
   public void setKnownUser(final boolean knownUser) {
      this.knownUser = knownUser;
   }
   public void setActiveUsersAtEventTime(final int activeUsersAtEventTime) {
      this.activeUsersAtEventTime = activeUsersAtEventTime;
   }
   public void setKnownUsersAtEventTime(final int knownUsersAtEventTime) {
      this.knownUsersAtEventTime = knownUsersAtEventTime;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + eventDateTime.hashCode();
      result = (prime * result) + reason.hashCode();
      result = (prime * result) + userName.hashCode();
      return result;
   }

   @Override
   public boolean equals(final Object other) {
      return (this == other) ||
             ((other != null) &&
              (other instanceof LoginEvent) &&
              (reason == ((LoginEvent) other).getReason()) &&
              userName.equals(((LoginEvent) other).getUserName()) &&
              eventDateTime.equals(((LoginEvent) other).getEventDateTime()));
   }
}
