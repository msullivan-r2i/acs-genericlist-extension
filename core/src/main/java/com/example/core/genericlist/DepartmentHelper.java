/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.core.genericlist;


import com.adobe.acs.commons.genericlists.GenericList;
import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DepartmentHelper extends WCMUsePojo {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentHelper.class);

    private String value;

    private String path;

    public Department getDepartment() {
        Iterator<Resource> list = getList();
        while (list.hasNext()) {
            Resource resource = list.next();

            Department department = resource.adaptTo(Department.class);
            logger.error("Department Value" + department.getValue());
            if (department.getValue().equals(this.value)) {
                return department;
            }
        }
        return null;
    }

    public List<Department> getAllDepartments() {

        Iterator<Resource> list = getList();
        List<Department> departments = new LinkedList<Department>();
        while (list.hasNext()) {
            Resource resource = list.next();
            Department department = resource.adaptTo(Department.class);
            departments.add(department);
        }
        return departments;
    }

    private Iterator<Resource> getList() {
        PageManager pageManager = getResourceResolver().adaptTo(PageManager.class);
        Page listPage = pageManager.getPage("/etc/acs-commons/lists/departments");

        if (listPage != null
                && listPage.getContentResource() != null
                && listPage.getContentResource().getChild("list") != null) {
            Iterator<Resource> listIterator = listPage.getContentResource().getChild("list").listChildren();
            return listIterator;
        }
        return null;
    }

    @Override
    public void activate() {
        this.value = get("value", String.class);
    }
}
