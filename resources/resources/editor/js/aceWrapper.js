/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

//URLs filled by backend
var ExternalEditor = {
  EXT_EDITOR: null,
  CAN_EDIT_URL: null,
  GET_FILE_URL: null,
  SAVE_FILE_URL: null,
  STATUS: {
    OK: "ok",
    ERROR: "error"
  },
  EXT_EDITOR: null,
};

//ACE wrapper
var CodeEditor = function() {
 return {
  MODES: {
    JAVASCRIPT: 'javascript',
    CSS: 'css',
    XML: 'xml'
  },
  MODE_BASE : 'ace/mode/',
  DEFAULT_MODE: 'text',
  DEFAULT_THEME_PATH : 'ace/theme/textmate',

  modeMap:
  { //highlight modes
    'coffee': 'coffee',
    'css': 'css',
    'html': 'html',
    'cdv': 'javascript',
    'javascript': 'javascript',
    'js': 'javascript',
    'cdfde': 'json',
    'json': 'json',
    'jsp': 'jsp',
    'less': 'less',
    'pgsql': 'pgsql',
    'properties': 'properties',  
    'sql': 'sql',
    'svg': 'svg',
    'kdb': 'text',
    'md': 'text',
    'mdx': 'text',
    'txt': 'text',
    'cda': 'xml',
    'ktr': 'xml',
    'xaction': 'xml',
    'xcdf': 'xml',
    'xjpivot': 'xml',
    'xml': 'xml',
    'wcdf': 'xml',
    'xq':'xquery',
    'xqy':'xquery',
    'xquery':'xquery'
  },

  mode: 'javascript',
  editor: null,
  editorId: null,

  initEditor: function(editorId) {
    this.editor = ace.edit(editorId); 
    this.editorId = editorId;
    this.setMode(null);
    //this.setTheme(null);
    this.setTheme(this.DEFAULT_THEME_PATH);
  },
  
  loadFile: function(filename, callback, errorCallback) {
    var myself = this;

    //check read permissions
    $.get(ExternalEditor.CAN_READ_URL, {path: filename}, function(response) {
        if(response && response.toString() === "true") {

          $.get(ExternalEditor.CAN_EDIT_URL, {path: filename}, function(response) {
            if(response && response.toString() === "true") {
              myself.setReadOnly(false);
            } else {
              myself.setReadOnly(true);
            }

            $.get(ExternalEditor.GET_FILE_URL, {path: filename}, function(response) {
              callback(response, filename);
            })
              .fail(function(response) {
                errorCallback(response);
              });
          })
            .fail(function(response) {
              errorCallback(response);
            });
          
        } else {
          errorCallback("Sorry, you don't have permissions to read " + filename);
        }
    })
      .fail(function(response) {
        errorCallback(response);
      });
  },

  setContents: function(contents) {
    this.editor.getSession().setValue(contents);
    $('#' + this.editorId).css("font-size", "12px");
    //this.editor.gotoLine(2);
    //document.getElementById('codeArea').style.fontSize='12px';

    //this.editor.navigateFileStart();
  },

  saveFile: function(filename, callback, errorCallback) {
    if(this.isReadOnly()) {
      errorCallback("Sorry, you don't have permissions to edit " + filename);
      return;
    }
    $.ajax({
      url: ExternalEditor.SAVE_FILE_URL,
      type: "POST",
      //contentType: "application/json",
      dataType: "json",
      data: {path: filename, data: this.getContents()},
      success: function(response) {
        callback(response);
      },
      error: function(response) {
        errorCallback(response);
      }
    });
  },
  
  getContents: function() {
    return this.editor.getSession().getValue();
  },
  
  setMode: function(mode) {
    if(this.editor == null) { return; }

    this.mode = this.modeMap[mode];

    var HLMode;
    if(this.mode == null) {
      HLMode = ace.require(this.MODE_BASE + this.DEFAULT_MODE).Mode;
    } else {
      HLMode = ace.require(this.MODE_BASE + this.mode).Mode;
    }
    this.editor.getSession().setMode(new HLMode());
  },

  getMode: function() {
    //return this.editor.getSession().$modeId;
    //return this.editor.getOption('mode');
    return this.mode;
  },
  
  setTheme: function(theme) {
    this.editor.setTheme((theme == null || theme == undefined)
      ? this.DEFAULT_THEME_PATH
      : theme);
  },

  getTheme: function() {
    return this.editor.getOption('theme');
  },
  
  setReadOnly: function(readOnly) {
    if(readOnly == this.editor.getReadOnly()) {
      return;
    } else {
      this.editor.setReadOnly(readOnly);
    }
  },
  
  isReadOnly: function() {
    return this.editor.getReadOnly();
  },
  
  insert: function(text) {
    this.editor.insert(text);
  },
  
  getEditor: function() {
    return this.editor;
  },
  
  onChange: function(callback) {
    this.editor.getSession().on('change', callback);
  }
 }//return new
};
