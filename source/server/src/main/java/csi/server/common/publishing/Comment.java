package csi.server.common.publishing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.security.Authorization;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Comment implements Comparable<Comment> {

    @Id
    @GeneratedValue
    protected Long id;

    @Column(nullable = false)
    protected String creator;

    @Column(nullable = false)
    protected Date timeStamp;

    @Column(nullable = false, length = 16384)
    protected String text;

    public Comment() {
        // Hibernate needs a default c'tor.
    }

    public Comment(String text, Authorization creator) {
        this.creator = creator.getName();
        this.text = text;
        this.timeStamp = new Date();
    }

    @Override
    public int compareTo(Comment that) {
        // Reverse order by date: most recent first.
        return that.timeStamp.compareTo(this.timeStamp);
    }

    public Long getId() {
        return id;
    }

    public String getCreator() {
        return creator;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof Comment) && this.timeStamp.getTime() == ((Comment) that).timeStamp.getTime() && this.creator.equals(((Comment) that).creator)
                && this.text.equals(((Comment) that).text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
