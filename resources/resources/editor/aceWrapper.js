/*!
 * Copyright 2002 - 2017 Webdetails, a Pentaho company.  All rights reserved.
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

define([
  "require", "exports", "module",
  "cte/lib/jquery",
  "ace/ace",
  "ace/mode-coffee",
  "ace/mode-css",
  "ace/mode-html",
  "ace/mode-javascript",
  "ace/mode-json",
  "ace/mode-jsp",
  "ace/mode-less",
  "ace/mode-pgsql",
  "ace/mode-properties",
  "ace/mode-sql",
  "ace/mode-svg",
  "ace/mode-text",
  "ace/mode-xml",
  "ace/mode-xquery",
  "ace/theme-textmate"
], function(require, exports, module, $, ace) {

  //URLs filled by backend
  var ExternalEditor = {
    LANG_PATH: "/pentaho/api/repos/cte/resources/editor/languages/",
    LOCALE: "en_US",
    PROVIDERS: "/pentaho/plugin/cte/api/providers",
    SAVE_FILE_URL: "/pentaho/plugin/cte/api/saveFile",
    EXT_EDITOR: "/pentaho/plugin/cte/api/edit",
    CAN_EDIT_URL: "/pentaho/plugin/cte/api/canEdit",
    CAN_READ_URL: "/pentaho/plugin/cte/api/canRead",
    GET_FILE_URL: "/pentaho/plugin/cte/api/getFile",
    STATUS: {
      OK: "ok",
      ERROR: "error"
    }
  };

  return function() {
    return {
      MODES: {
        JAVASCRIPT: 'javascript',
        CSS: 'css',
        XML: 'xml'
      },
      MODE_BASE : 'ace/mode/',
      DEFAULT_MODE: 'text',
      DEFAULT_THEME_PATH : 'ace/theme/textmate',

      modeMap: { //highlight modes
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

      initEditor: function(editorId, saveFunc) {
        this.editor = ace.edit(editorId);
        this.editorId = editorId;
        this.setMode(null);
        //this.setTheme(null);
        this.setTheme(this.DEFAULT_THEME_PATH);
        // save command key bindings
        this.editor.commands.addCommand({
          name: 'saveFile',
          bindKey: {
            win: 'Ctrl-S',
            mac: 'Command-S',
            sender: 'editor|cli'
          },
          exec: function(env, args, request) {
            saveFunc();
          }});
      },

      loadFile: function(filename, provider, callback, errorCallback) {
        var myself = this;

        //check read permissions
        $.get(ExternalEditor.CAN_READ_URL + "?ts=" + (new Date).getTime(), {path: filename, provider: provider} , function(response) {
          if(response && response.toString() === "true") {

            $.get(ExternalEditor.CAN_EDIT_URL + "?ts=" + (new Date).getTime(), {path: filename, provider: provider}, function(response) {
              if(response && response.toString() === "true") {
                myself.setReadOnly(false);
              } else {
                myself.setReadOnly(true);
              }

              $.get(ExternalEditor.GET_FILE_URL + "?ts=" + (new Date).getTime(), {path: filename, provider: provider}, function(response) {
                callback(response, filename, provider );
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

      saveFile: function(filename, provider, callback, errorCallback) {
        if(this.isReadOnly()) {
          errorCallback("Sorry, you don't have permissions to edit " + filename);
          return;
        }
        $.ajax({
          url: ExternalEditor.SAVE_FILE_URL + "?ts=" + (new Date).getTime(),
          type: "POST",
          //contentType: "application/json",
          dataType: "json",
          data: {path: filename, provider: provider, data: this.getContents()},
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
        if(this.editor == null || this.mode === mode) { return; }

        this.mode = this.modeMap[mode];

        var _self = this;

        require([this.MODE_BASE + (this.mode == null ? this.DEFAULT_MODE : this.mode)], function(NewMode) {
          //clear previous code annotations
          _self.editor.getSession().clearAnnotations();
          _self.editor.getSession().setMode(new NewMode.Mode());
        }, function(error) {
          console && console.log(error.message);
        });
      },

      getMode: function() {
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
      },

      getProviders: function( callback, errorCallback ) {
        var myself = this;

        $.ajax({
          url: ExternalEditor.PROVIDERS + "?ts=" + (new Date).getTime(),
          type: "GET",
          dataType: "json",
          async: false,
          success: function(response) {
            callback( response );
          },
          error: function( response ) {
            errorCallback( response );
          }
        });
      }
    }; // return
  }; // return
});
