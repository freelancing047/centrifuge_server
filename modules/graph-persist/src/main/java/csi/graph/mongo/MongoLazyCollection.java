package csi.graph.mongo;

import java.util.AbstractCollection;
import java.util.Iterator;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Collection class for MongoDB. This collection wraps the cursor returned
 * from a query.  
 * 
 * Implementation note:  do not use standard collection 
 * 
 */
public class MongoLazyCollection
    extends AbstractCollection<DBObject>
{
    
    DBCursor cursor;

    public MongoLazyCollection(DBCursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public Iterator iterator() {
//        return new MongoIterator();
        return cursor.copy();
    }

    @Override
    public int size() {
        return cursor.size();
    }

    class MongoIterator
        implements Iterator<DBObject>
    {

        @Override
        public boolean hasNext() {
            boolean hasNext = cursor.hasNext();
            return hasNext;
        }

        @Override
        public DBObject next() {
            DBObject next = cursor.next();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
