<#--

Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)     
                                                                           
This file is part of greenDAO Generator.                                   
                                                                           
greenDAO Generator is free software: you can redistribute it and/or modify 
it under the terms of the GNU General Public License as published by       
the Free Software Foundation, either version 3 of the License, or          
(at your option) any later version.                                        
greenDAO Generator is distributed in the hope that it will be useful,      
but WITHOUT ANY WARRANTY; without even the implied warranty of             
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              
GNU General Public License for more details.                               
                                                                           
You should have received a copy of the GNU General Public License          
along with greenDAO Generator.  If not, see <http://www.gnu.org/licenses/>.

-->
package ${schema.defaultJavaPackageDao};

import android.content.Context;
import de.greenrobot.dao.wrapper.SQLiteDatabaseWrapper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import de.greenrobot.dao.wrapper.SQLiteOpenHelperWrapper;
import android.util.Log;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import info.guardianproject.cacheword.CacheWordHandler;

<#list schema.entities as entity>
import ${entity.javaPackageDao}.${entity.classNameDao};
</#list>

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * Master of DAO (schema version ${schema.version?c}): knows all DAOs.
*/
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = ${schema.version?c};

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(SQLiteDatabaseWrapper db, boolean ifNotExists) {
<#list schema.entities as entity>
<#if !entity.skipTableCreation>
        ${entity.classNameDao}.createTable(db, ifNotExists);
</#if>
</#list>
    }
    
    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(SQLiteDatabaseWrapper db, boolean ifExists) {
<#list schema.entities as entity>
<#if !entity.skipTableCreation>
        ${entity.classNameDao}.dropTable(db, ifExists);
</#if>
</#list>
    }
    
    public static abstract class OpenHelper extends SQLiteOpenHelperWrapper  {

        public OpenHelper(Context context, String name,CacheWordHandler cacheWord, CursorFactory factory, boolean cryptDB) {
            super(context, name, cacheWord, factory, SCHEMA_VERSION, cryptDB);
        }

        @Override
        public void onCreate(SQLiteDatabaseWrapper db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }
    
    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name, CacheWordHandler cacheWord, CursorFactory factory, boolean cryptDB) {
            super(context, name, cacheWord, factory, cryptDB);
        }

        @Override
        public void onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

    public DaoMaster(SQLiteDatabaseWrapper db) {
        super(db, SCHEMA_VERSION);
<#list schema.entities as entity>
        registerDaoClass(${entity.classNameDao}.class);
</#list>
    }
    
    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }
    
    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }
    
}
