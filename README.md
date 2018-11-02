# Extending the AEM ACS Commons Generic List to Support Multiple Fields

The Adobe Experience Manager ACS Commons Generic List facilitates authorable Title / Value pairs that can be used to populate drop downs fro a centrally authored location.  The functionality of the Generic List is limited to the Title and Value pairs, but there could be other use cases for centrally managed metadata that can be easily chosen from a dropdown.  This example will extend the Generic List to manage metadata for organizational units / departments.

The Generic List implementation is final, so not extendable, but the underlying interface only relies on the `jcr:title`/`value` pairs to be present.  We will use this and add additional fields that we can adaptTo our own implementation.  The Generic List was built before the prevalence of Sling Models, so for this example we will try to simplify this by utilizing Sling Models.

## Implementing the List Extension

*department.java*

This is the Sling Model that will add `email` and `phone` values to the existing `jcr:title` and `value` fields.  It implements GenericList.Item, however this is not strictly required.  The mechanisms that populate the dropdown will not use the `Department` implementation and rely on they underlying `jcr:title` & `value` fields and utilize the ACS implementation.

```java
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
        /* see full file */
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
```

*deparment-generic-list.html*

This renders the item in the Generic List authoring page.  The ACS Implementation uses JSP and not Sling Models, but this is a little simpler.

```html
<sly data-sly-use.department="com.example.core.genericlist.Department">
    <li>
        <span data-sly-test="${!department.title}" style="color: red;">Please enter a title</span>
        <sly data-sly-test="${department.title}">
            Title: ${department.title} <br />
            Value: ${department.value} <br />
            Phone: ${department.phone} <br />
            Value: ${department.email} <br />
        </sly>
    </li>
</sly>

```

*dialog.xml*

The Generic List is still Classic UI only, so we keep the `jcr:title` and `value` fields and add our `email` and `phone` fields.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
    jcr:primaryType="cq:Dialog"
    title="Department - Generic List Item"
    xtype="panel">
    <items jcr:primaryType="cq:WidgetCollection">
        <title
            jcr:primaryType="cq:Widget"
            fieldLabel="Title"
            name="./jcr:title"
            xtype="textfield"/>
        <value
            jcr:primaryType="cq:Widget"
            fieldDescription="This is typically the text used internally or for URL generation"
            fieldLabel="Value"
            name="./value"
            xtype="textfield"/>
        <phone
            jcr:primaryType="cq:Widget"
            fieldLabel="Phone"
            name="./phone"
            xtype="textfield"/>
        <email
            jcr:primaryType="cq:Widget"
            fieldLabel="Email Address"
            name="./email"
            xtype="textfield"/>
    </items>
    <listeners
        jcr:primaryType="nt:unstructured"
        afterrender="function() { ACS.CQ.GenericListItem.addTitleFields(this); }"/>
</jcr:root>
```

## Optional Implementation

The example also creates a new template that can be placed under `/etc/acs-commons/genericlist` and an `/etc/designs/acs-genericlist-example` design definition that allows our `department-generic-list` component to be added to that page template.  There is also a page component that simply extends the acs genericlist page component to give the design something unique to bind to.

This is optional and the extended generic list item could be allowed through the standard design ui.


## Usage


### Department Helper

The department helper provides two methods, one to get a specific `Department` model given a value.  The other to give a `List<Department>` of all of the models.


A `department` and a `departments` sample component are in the example project.

### Department Component

The `department` component shows how to implement the dialog field to allow an author to choose a department from a dropdown.  It then utilizes a WCMUsePojo helper to display the details of the selected department.

*department/cq:dialog*

We are referencing the ACS `genericlist/datasource` ui component to populate the dropdown, so this requires `jcr:title` and `value` fields be present for each item.

```xml
<department
    jcr:primaryType="nt:unstructured"
    sling:resourceType="granite/ui/components/coral/foundation/form/select"
    fieldLabel="Department"
    name="./department">
    <datasource
        jcr:primaryType="nt:unstructured"
        sling:resourceType="acs-commons/components/utilities/genericlist/datasource"
        path="/etc/acs-commons/lists/departments" />
</department>
```

*department.html*

The DepartmentHelper takes the value of the selected department from the dropdown and returns an adapted Depertment model.

```html
<div class="cmp-department"
     data-sly-use.departmentHelper="${'com.example.core.genericlist.DepartmentHelper' @ value=properties.department }"
     data-sly-test.department="${departmentHelper.department}">
    <h1>${department.title}</h1>
    <ul>
        <li>${department.phone}</li>
        <li>${department.email}</li>
    </ul>
</div>
<h1 data-sly-test="${!department}">Select a Department</h1>
```

### Departments Component

The `departments` component iterates over all of the departments in the list in node order.

*departments.html*

```html
<div class="cmp-departments"
     data-sly-use.departmentHelper="com.example.core.genericlist.DepartmentHelper"
     data-sly-test.departments="${departmentHelper.allDepartments}"
     data-sly-test.hasDepartments="${departments.size > 0}">
    <div data-sly-list.department="${departments}">
        <h1>${department.title}</h1>
        <ul>
            <li>${department.phone}</li>
            <li>${department.email}</li>
        </ul>
    </div>
</div>
<h1 data-sly-test="${!hasDepartments}">Add departments to the department generic list named <pre>/etc/acs-commons/lists/departments</pre></h1>
```

## I18n

This implementation doesn't conside I18n, and the ACS list seems to do this through the dictionary.  If this is a requirement, perhaps moving these out of `/etc` and into `/content/../locale` could allow for translations of this metadata as long as the values stayed the same.




## Building the Sample AEM project template

This is a project template for AEM-based applications. It is intended as a best-practice set of examples as well as a potential starting point to develop your own functionality.

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs as well as Hobbes-tests

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with  

    mvn clean install -PautoInstallPackage
    
Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish
    
Or alternatively

    mvn clean install -PautoInstallPackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
