/*
 * Copyright 2011 SUSE Linux Products GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.suse.logkeeper.validators;

import de.suse.logkeeper.plugins.SpecValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bo
 */
public class SpacewalkValidator implements SpecValidator {
    public static final String[] USER = new String[] {"USR.ID", "USR.ORGID"};
    public static final String REQUEST = "REQ";

    public static final String[] EVENT = new String[] {"EVT.SRC", "EVT.TYPE", "EVT.URL"};
    public static final String[] EVENT_SOURCES = new String[] {
        "WEBUI", "CLI", "COBBLER", "FRONTEND_API", "BACKEND_API", "URL"
    };

    public static final String[] EVENT_TYPES = new String[] {
        "ADD_USER", "EDIT_USER", "REMOVE_USER",
        "ASSIGN_GROUP_ROLE", "REVOKE_GROUP_ROLE", "USER_CREDENTIAL_UPDATE",
        "LOGIN", "LOGOUT",
        "ACTION_DELETE", "ACTION_CREATE", "ACTION_CHANGE",
    };

    /**
     * Validate extmap.
     * 
     * @param extmap
     * @return
     */
    public List<String> inspect(Map<String, String> extmap) {
        String[][] validators = new String[][] {
            SpacewalkValidator.USER,
            SpacewalkValidator.EVENT,
        };

        String[][] actValidators = new String[][] {
            SpacewalkValidator.EVENT_SOURCES,
            SpacewalkValidator.EVENT_TYPES,
        };
        
        List<String> failed = new ArrayList<String>();
        boolean keyFailed = true;
        for (String key : extmap.keySet()) {
            keyFailed = true;
            for (int i = 0; i < validators.length; i++) {
                if (key.startsWith(SpacewalkValidator.REQUEST + ".") || this.isIn(key, validators[i])) {
                    keyFailed = false;
                    break;
                }
            }

            if (keyFailed) {
                failed.add(key);
            } else {
                if (key.startsWith("EVT.") && !key.equals("EVT.URL")) {
                    boolean actFailed = true;
                    for (int i = 0; i < actValidators.length; i++) {
                        String action = (String) extmap.get(key);
                        if (this.isIn(action, actValidators[i])) {
                            actFailed = false;
                            break;
                        }
                    }
                    if (actFailed) {
                        failed.add(key);
                    }
                }
            }
        }

        return failed;
    }

    /**
     * Find failing keys.
     *
     * @param extmap
     * @return
     */
    public boolean validate(Map<String, String> extmap) {
        return this.inspect(extmap).isEmpty();
    }


    private boolean isIn(String value, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (value.equals(array[i])) {
                return true;
            }
        }

        return false;
    }
}
