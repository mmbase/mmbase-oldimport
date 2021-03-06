/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.storage.search.implementation.database;

import org.mmbase.bridge.Field;
import org.mmbase.storage.search.*;
import org.mmbase.storage.implementation.database.DatabaseStorageManager;
import org.mmbase.util.logging.*;
import org.mmbase.util.LocaleCollator;
import org.mmbase.module.core.MMBase;
import org.mmbase.module.core.MMObjectBuilder;
import org.mmbase.core.CoreField;
import org.mmbase.datatypes.*;
import java.util.regex.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The MySQL query handler, implements {@link
 * org.mmbase.storage.search.implementation.database.SqlHandler SqlHandler} for standard
 * MySQL functionality.
 * <br />
 * Derived from {@link BasicSqlHandler BasicSqlHandler}, overrides
 * <ul>
 * <li>{@link #toSql toSql()}, implements {@link
 * org.mmbase.storage.search.SearchQueryHandler#FEATURE_MAX_NUMBER
 * FEATURE_MAX_NUMBER} and {@link
 * org.mmbase.storage.search.SearchQueryHandler#FEATURE_OFFSET
 * FEATURE_OFFSET}, by adding a construct like "<code>LIMIT 20</code>" or
 * "<code>LIMIT 80, 20</code>" after the body, when appropriate.
 * <li>{@link #getSupportLevel(int,SearchQuery) getSupportLevel(int,SearchQuery)},
 * returns {@link
 * org.mmbase.storage.search.SearchQueryHandler#SUPPORT_OPTIMAL
 * SUPPORT_OPTIMAL} for these features, delegates to the superclass for
 * other features.
 * </ul>
 *
 * @author Rob van Maris
 * @version $Id$
 * @since MMBase-1.7
 */
public class MySqlSqlHandler extends BasicSqlHandler implements SqlHandler {

    private static final Logger log = Logging.getLoggerInstance(MySqlSqlHandler.class);

    /**
     * Constructor.
     */
    public MySqlSqlHandler() {
        super();
    }

    @Override
    protected String toSqlString(String str) {
        //http://dev.mysql.com/doc/refman/5.0/en/string-syntax.html
        String res =  super.toSqlString(str
                                        .replaceAll("\\\\", "\\\\\\\\")
                                        .replaceAll("\t", "\\\\t")
                                        .replaceAll("\0", "\\\\0")
                                        .replaceAll("\b", "\\\\b")
                                        .replaceAll("\32", "\\\\Z")
                                        );
        return res;
    }

    // javadoc is inherited
    @Override
    public int getSupportLevel(int feature, SearchQuery query) throws SearchQueryException {
        int result;
        switch (feature) {
        case SearchQueryHandler.FEATURE_MAX_NUMBER:
            result = SearchQueryHandler.SUPPORT_OPTIMAL;
            break;

        case SearchQueryHandler.FEATURE_OFFSET:
            result = SearchQueryHandler.SUPPORT_OPTIMAL;
            break;
        case SearchQueryHandler.FEATURE_REGEXP:
            result = SearchQueryHandler.SUPPORT_OPTIMAL;
            break;
        default:
            result = super.getSupportLevel(feature, query);
        }
        return result;
    }


    protected CoreField getField(StepField field) {
        String stepName = field.getStep().getTableName();
        String fieldName = field.getFieldName();
        MMBase mmb = MMBase.getMMBase();
        DatabaseStorageManager storageManager = (DatabaseStorageManager) mmb.getStorageManagerFactory().getStorageManager();
        MMObjectBuilder bul = mmb.getBuilder(stepName);
        return bul.getField(fieldName);
    }
    protected DataType getDataType(StepField field) {
        return getField(field).getDataType();
    }

    protected boolean isCaseSensitive(DataType dt) {
       if (dt instanceof StringDataType) {
           StringDataType sdt = (StringDataType) dt;
           if (sdt.getCollator().getStrength() == LocaleCollator.Strength.IDENTICAL.get()) {
               return true;
           }
       }
       return false;
    }

    private static final Pattern NO_LOWER_NEEDED = Pattern.compile("(varchar|char|text|mediumtext|longtext).*");
    private static final Map<String, Boolean> useLowerCache = new ConcurrentHashMap<String, Boolean>();
    // javadoc inherited
    @Override
    protected boolean useLower(FieldConstraint constraint) {
        StepField stepField = constraint.getField();
        String stepName = stepField.getStep().getTableName();
        String fieldName = stepField.getFieldName();
        String key = stepName + ":" + fieldName;
        Boolean res = useLowerCache.get(key);
        if (res == null) {
            if (isCaseSensitive(getDataType(stepField))) {
                res = true;
            } else {
                MMBase mmb = MMBase.getMMBase();
                DatabaseStorageManager storageManager = (DatabaseStorageManager) mmb.getStorageManagerFactory().getStorageManager();
                String fieldDef = storageManager.getFieldTypeDefinition(getField(stepField));
                res = ! NO_LOWER_NEEDED.matcher(fieldDef).matches();
            }
            useLowerCache.put(key, res);
        }
        return res;
    }


    /**
     * @since MMBase-1.9.2
     */
    @Override
    protected String appendPreField(StringBuilder sb, FieldConstraint constraint, StepField field, boolean multiple) {
        if (constraint instanceof FieldCompareConstraint) {
            FieldCompareConstraint compare = (FieldCompareConstraint) constraint;
            if (field.getType() == Field.TYPE_STRING && compare.isCaseSensitive() && ! isCaseSensitive(getDataType(field))) {
                sb.append(" BINARY ");
            }
        }
        return null;
    }

    @Override
    protected StringBuilder appendLikeOperator(StringBuilder sb, FieldConstraint constraint) {
        if (constraint.isCaseSensitive() && ! isCaseSensitive(getDataType(constraint.getField()))) {
            sb.append(" LIKE BINARY ");
        } else {
            sb.append(" LIKE ");
        }
        return sb;
    }

    @Override
    protected StringBuilder appendRegularExpressionOperator(StringBuilder sb, FieldConstraint constraint) {
        if (constraint.isCaseSensitive() && ! isCaseSensitive(getDataType(constraint.getField()))) {
            sb.append(" REGEXP BINARY ");
        } else {
            sb.append(" REGEXP ");
        }
        return sb;
    }

    /**
     * @javadoc
     */
    @Override
    protected void appendDateField(StringBuilder sb, Step step, String fieldName, boolean multipleSteps, int datePart) {
        String datePartFunction = null;
        switch (datePart) {
        case FieldValueDateConstraint.CENTURY:
            datePartFunction = "CENTURY";
            break;
        case FieldValueDateConstraint.QUARTER:
            datePartFunction = "QUARTER";
            break;
        case FieldValueDateConstraint.WEEK:
            datePartFunction = "WEEK";
            break;
        case FieldValueDateConstraint.DAY_OF_YEAR:
            datePartFunction = "DAYOFYEAR";
            break;
        case FieldValueDateConstraint.DAY_OF_WEEK:
            datePartFunction = "DAYOFWEEK";
                break;
        default:
            log.debug("Unknown datePart " + datePart);
        }
        if (datePartFunction != null) {
            sb.append(datePartFunction);
            sb.append("(");
            appendField(sb, step, fieldName, multipleSteps);
            sb.append(")");
        } else {
            super.appendDateField(sb, step, fieldName, multipleSteps, datePart);
        }
    }
    @Override
    protected StringBuilder appendSortOrderField(StringBuilder sb, SortOrder sortOrder, boolean multipleSteps) {
        if (sortOrder.isCaseSensitive() && sortOrder.getField().getType() == Field.TYPE_STRING) {
            sb.append("BINARY ");
        }
        // Fieldname.
        appendField(sb, sortOrder, multipleSteps);
        return sb;
    }

    // javadoc is inherited
    @Override
    public String toSql(SearchQuery query, SqlHandler firstInChain) throws SearchQueryException {
        // XXX should table and field aliases be tested for uniqueness?

        // Test for at least 1 step and 1 field.
        if (query.getSteps().isEmpty()) {
            throw new IllegalStateException("Searchquery has no step (at least 1 step is required).");
        }
        if (query.getFields().isEmpty()) {
            throw new IllegalStateException("Searchquery has no field (at least 1 field is required).");
        }

        // SELECT
        StringBuilder sbQuery = new StringBuilder("SELECT ");

        // DISTINCT
        if (query.isDistinct()) {
            sbQuery.append("DISTINCT ");
        }

        firstInChain.appendQueryBodyToSql(sbQuery, query, firstInChain);

        // LIMIT
        if (query.getMaxNumber() != -1) {
            // Maxnumber set.
            sbQuery.append(" LIMIT ");
            if (query.getOffset() != 0) {
                sbQuery.append(query.getOffset()).
                append(",");
            }
            sbQuery.append(query.getMaxNumber());
        } else {
            // Offset > 0, maxnumber not set.
            if (query.getOffset() != 0) {
                sbQuery.append(" LIMIT ").
                append(query.getOffset()).
                append(",").
                append(Integer.MAX_VALUE);
            }
        }

        String strSQL = sbQuery.toString();
        if (log.isDebugEnabled()) {
            log.debug("generated SQL: " + strSQL);
        }
        return strSQL;
    }
}
