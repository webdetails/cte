Community Text Editor
=====================

Objective
---------

The goal of this project is provide the ability to edit the contents of a file within the JCR repository, directly from the PUC ( Pentaho User Console )

Getting started
---------------

The first thing you should do is to confirm you have ant installed ( http://ant.apache.org/ ).

To prep the project, you first need to resolve/include the necessary dependencies.
Ivy.xml file contains a list of the dependencies needed to successfully compile this project.

### To fetch dependencies and populate /lib folder 

From the project root and using command-line simply type *ant resolve*


How to use
----------

Following this steps should get you going:

### Compile the project

Just run *ant dist* and you should be all set


### Deploying the plugin in your pentaho environment

Copy the zip folder in ./dist folder and unzip it at 

pentaho-solutions/system/


