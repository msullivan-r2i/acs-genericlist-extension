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
import com.adobe.acs.commons.genericlists.impl.GenericListImpl;
import com.day.cq.wcm.api.NameConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.settings.SlingSettingsService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Locale;

@Model(adaptables=Resource.class)
public class Department implements GenericList.Item {

    static final String TITLE_PREFIX = NameConstants.PN_TITLE + ".";

    @Inject
    private Resource resource;

    @Inject @Named("jcr:title") @Default(values="")
    public String title;

    @Inject @Default(values="")
    public String value;

    @Inject @Default(values="")
    public String phone;

    @Inject @Default(values="")
    public String email;

    public String getTitle() {
        return title;
    }

    public String getTitle(Locale locale) {
        // no locale - return default title
        if (locale == null) {
            return getTitle();
        }

        // no language - return default title
        String language = locale.getLanguage();
        if (language.length() == 0) {
            return getTitle();
        }

        String localizedTitle = null;

        // try property name like jcr:title.de_ch
        if (locale.getCountry().length() > 0) {
            localizedTitle = getLocalizedTitle(locale);
        }
        // then just jcr:title.de
        if (localizedTitle == null) {
            localizedTitle = getLocalizedTitle(new Locale(language));
        }
        if (localizedTitle == null) {
            return getTitle();
        } else {
            return localizedTitle;
        }
    }

    private String getLocalizedTitle(Locale locale) {
        return resource.getValueMap().get(TITLE_PREFIX + locale.toString().toLowerCase(), String.class);
    }

    public String getValue() {
        return value;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
