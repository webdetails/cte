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

  modeMap :
  { //highlight modes
    'css': 'css',
    'cdfde': 'javascript',
    'cdv': 'javascript',
    'javascript': 'javascript',
    'js': 'javascript',
    'json': 'javascript',
    'html': 'html',
    'sql': 'text',
    'txt': 'text',
    'properties': 'text',
    'md': 'text',
    'mdx': 'text',
    'cda': 'xml',
    'ktr': 'xml',
    'xcdf': 'xml',
    'xaction': 'xml',
    'xjpivot': 'xml',
    'xml': 'xml',
    'wcdf': 'xml'

  },

  mode: 'javascript',
  theme: 'ace/theme/textmate',
  editor: null,
  editorId: null,

  initEditor: function(editorId) {
  this.editor = ace.edit(editorId); 
  this.editorId = editorId;
  this.setMode(null);
  this.setTheme(null);

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

  /*

    //check edit permission
    $.get(ExternalEditor.CAN_EDIT_URL, {path: filename},
      function(result) {
        debugger;
        var readonly = !(result == "true");
        myself.setReadOnly(readonly);
        //TODO: can read?..get permissions?...

        //load file contents
        //$.get(ExternalEditor.GET_FILE_URL, {path: filename}).done(callback, filename).fail(errorCallback);
        $.ajax({
          url: ExternalEditor.GET_FILE_URL,
          type: "GET",
          //contentType: "application/json",
          //dataType: "json",
          data: {path: filename},
          success: function(response) {
            callback(response, filename);
          },
          error: function(response) {
            errorCallback(response);
          }
        });

      });
    */
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
    this.mode = this.modeMap[mode];

    if(this.mode == null) {
      this.mode = this.DEFAULT_MODE;
    }

    if(this.editor != null) {
      if(this.mode != null) {
        var HLMode = ace.require(this.MODE_BASE + this.mode).Mode;
        this.editor.getSession().setMode(new HLMode());
      }
    }
  },
  
  setTheme: function(themePath) {
    this.editor.setTheme((themePath == null || themePath == undefined) ? this.theme : themePath);
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
