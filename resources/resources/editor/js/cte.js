// TODO: refactor into modules and use requirejs to load them
// TODO: adapter pattern to provide an interface (load, save, ...) so other editors can be used (e.g. CodeMirror)

var editor = new CodeEditor(),
    editorMode = "";
// stores URL parameters
var params = {};
// stores currently opened file name
var fileName = "";
// stores currently opened file provider
var fileProvider = "";
// true if 'change' event was triggered, except when file is beeing loaded
var isDirty = false;
// if true isDirty will be set to true when 'change' events are triggered in the editor
var pageLoaded = false;

var listeners = {
  onStatusUpdate: null,
  onSave: null,
  notify: function(msg, type) { console.log(type + " : " + msg); }
};

var load = function(filename, provider) {

  editor.loadFile(
    filename,
    provider,
    function(response, loadedFileName, loadedFileProvider) {

      pageLoaded = false;

      // set filename in global var
      fileName = loadedFileName;

      // set file provider in global var
      fileProvider = loadedFileProvider;

      editor.setContents(response);

      pageLoaded = true;

      setDirty(false);

      // use menu selected mode or filename extension to set mode
      editor.setMode((!editorMode) ? loadedFileName.split('.').pop() : editorMode);

      notify('File ' + loadedFileName + ' loaded.');

      updateStatus();
    },
    function(response) {
      if(response) {
        if(typeof response.error == "function") {
          notify("Error loading file: " + response.error().statusText);
        } else {
          notify(response);
        }
      }
    }
  );
};

var save = function() {

  editor.saveFile(
    fileName, 
    fileProvider,
    function(response) {
      var msg = "";
      if(response && response.toString() === "true") {
        pageLoaded = true;
        setDirty(false);
        notify(fileName + ' saved.');
      } else {
        notify('Error saving file: ' + fileName, 'error');
      }
    },
    function(response) {
      notify(response, 'error');
    }
  );
};

var notifyType = {
  ERROR: 'error',
  INFO: 'info',
  WARN: 'warn'
};
var notify = function(msg, type) {
  if(type == null) {
    type = notifyType.INFO;
  }
  if(typeof(listeners.notify) == 'function') {
    listeners.notify(msg, type);
  }
};

var updateStatus = function() {
  //var infoWarning = (editor.isReadOnly() && pageLoaded ? ' (readonly)' : '');
  var infoWarning,
     $saveBtn = $("#saveBtn"),
     $infoArea = $('#infoArea');

  if(fileName && fileName.length > 0) {
    //$infoArea.removeClass("disabled");
    $infoArea.show();
  } else {
    //$infoArea.addClass("disabled");
    $infoArea.hide();
  }

  if(editor.isReadOnly()) {
    $saveBtn.addClass("disabled");
    $saveBtn.removeClass("btn-success");

    $infoArea.removeClass("alert-warning");
    $infoArea.removeClass("alert-success");
    $infoArea.addClass("alert-danger");

    if(fileName && fileName.length > 0) {
      $('#infoAreaWarnings').text('(readonly)');
    }
  } else {
    // manage enable/disable save button

    if(isDirty && !editor.isReadOnly()) {
      //$saveBtn.prop("disabled", false).css("cursor", "pointer").fadeTo(500, 1);
      $saveBtn.removeClass("disabled").fadeTo(500, 1);
      $saveBtn.addClass("btn-success");

      $infoArea.removeClass("alert-danger");
      $infoArea.removeClass("alert-success");
      $infoArea.addClass("alert-warning");

      $('#infoAreaWarnings').text('*');
    } else {
      //$saveBtn.prop("disabled", true).css("cursor", "default").fadeTo(500, 0.2);
      $saveBtn.addClass("disabled");
      $saveBtn.removeClass("btn-success");

      $infoArea.removeClass("alert-danger");
      $infoArea.removeClass("alert-warning");
      $infoArea.addClass("alert-success");

      $('#infoAreaWarnings').text('');
    }
  }

  //$infoArea.text(fileName);
  if(fileName.length > 50) {
    var _path = fileName.substring(fileName.length - 50, fileName.length);
    $infoArea.text("(...)" + _path.substring(_path.indexOf("/"), _path.length));
  } else {
    $infoArea.text(fileName);
  }

  if(typeof(listeners.onStatusUpdate) == 'function') {
    listeners.onStatusUpdate(isDirty);
  }
};

var setDirty = function(dirty) {
  var wasDirty = isDirty;
  isDirty = dirty;

  if(isDirty != wasDirty) {
    if(!wasDirty && isDirty) {
      setExitNotification(true);
    } else if(wasDirty && !isDirty) {
      setExitNotification(false);
    }
    updateStatus();
  }
};

var setExitNotification= function(enable) {
  if(window.parent && window.parent != window) {//only mess with unload if owning the window
    return;
  }
  
  if(enable) {
    window.onbeforeunload = function(e) {
      return 'Any unsaved changes will be lost.';
    }
  } else {
    window.onbeforeunload = function() {null};
  }
};




/*
 * window events
 */

$(window).load(function() {
  // read url query parameters
  var queryParam,
      queryParams = window.location.search.substring(1),
      myRegex = /([^&=]+)=?([^&]*)/g;

  while((queryParam = myRegex.exec(queryParams)) !== null) {
    params[decodeURIComponent(queryParam[1])] = decodeURIComponent(queryParam[2]);
  }

  
  if(params.editorOnly) {
    $('#btnArea').css('display', 'none');
    $('#editArea').css('margin-top', 0);
    $('#editArea').css('margin-bottom', 0);
  }
  
  if(params.height) {
    $('#editArea').height(params.height);
  } else {
    $('#fileTreeAccordion').height(window.innerHeight - (params.editorOnly ? 20 : 80));
    $('#editArea').height(window.innerHeight - (params.editorOnly ? 20 : 80));
  }
  
  if(params.width) {
    $('#editArea').width(params.width);
  } else{
    $('#fileTreeAccordion').width(window.innerWidth - (window.innerWidth * 0.75) - 25);
    $('#editArea').width('75%');
  }
      
  editor.initEditor('editArea', save);

  if(params.theme) {
    editor.setTheme(params.theme);
  }
  
  if(params.mode) {
    editor.setMode(params.mode);
  }

  editor.onChange(function(e) {
    //console.log(e.data.action);
    if(pageLoaded) {
      setDirty(true);
    }
  });

  // defaults settings
  var $saveBtn = $("#saveBtn");
  //$("#saveBtn").prop("disabled", true).css("cursor", "default").fadeTo(500, 0.2);
  $saveBtn.addClass("disabled").fadeTo(500, 0.2);;
  $saveBtn.removeClass("btn-success");
  editor.setReadOnly(true);

  if(params.path) {
    // More info, see aceWrapper.js
    load(params.path, params.provider);
  } else {
    fileName = "";//"Unsaved";
    updateStatus();
  }


  /********************
   **** FileTree ******
   ********************/

  editor.getProviders( function f( providers ){
    if( providers && providers.length > 0 ){
      for( i = 0; i < providers.length; i++ ){
        var providerId = providers[i].id;
        var providerName = providers[i].name;

        var $divTreePanel = $('<div class="panel panel-default">')
          .append('<div class="panel-heading"><h4 class="panel-title"><a data-toggle="collapse" data-parent="#fileTreeAccordion" href="#panel-' + providerId + '" >' + providerName + '</a></h4></div>');
        var $divTreeCollapsible = $('<div id="panel-' + providerId + '" class="panel-collapse collapse">');
        var $divTree = $('<div id="treeDiv-' + providerId + '"  provider-id="' + providerId + '"provider-name="' + providerName + '" class="urltargetfolderexplorer">');
        
        $divTree.fileTree({
          root: "/",
          providerId: providerId,
          providerName: providerName,
          script: "/pentaho/plugin/cte/api/tree?showHiddenFiles=true&provider=" + providerId,
          expandSpeed: 500,
          collapseSpeed: 500,
          multiFolder: false,
          folderClick: function(obj, folder) {
            if($(".selectedFolder").length > 0) {
              $(".selectedFolder").attr("class", "");
            }
            $(obj).attr("class", "selectedFolder");
          }
        },
        function(file, provider) {
          if(!isDirty
            || (isDirty && confirm("You have unsaved changes, discard them?"))) {

            $(".selectedFile").attr("class", "");
            $("a[rel='" + file + "']").attr("class", "selectedFile");

            load("/" + file, provider);
          }
          
        });

        $divTreeCollapsible.append($('<div class="panel-body">').append($divTree));
        $divTreePanel.append($divTreeCollapsible);
        $('#fileTreeAccordion').append($divTreePanel);
        
        $('.panel-title a').addClass('collapsed');
      }
    }
  });

  /**********************************
   **** editor mode select box ******
   **********************************/
  $("#modes")
    .change(function() {
      editorMode = $(this).val();
      // auto detect
      if(!editorMode) {
        editorMode = (!fileName) ? "" : fileName.split('.').pop();
      }
      editor.setMode(editorMode);
      notify("Requested editor mode: " + editorMode, 'info');
    });
    
});


/*
$(window).resize(function() {
  $('#fileTreeAccordion').height(window.innerHeight - (params.editorOnly ? 20 : 80));
  $('#editArea').height(window.innerHeight - (params.editorOnly ? 20 : 80));

  $('#fileTreeAccordion').width(window.innerWidth - (window.innerWidth * 0.75) - 25);
  $('#editArea').width(window.innerWidth - (window.innerWidth * 0.25) - 25);
});
*/

$(function() {
  $(".syntaxSelector").select2({width: '120px'});
});

function preload(sources)
{
  jQuery.each(sources, function(i,source) { jQuery.get(source); });
}


$(function(){
    $('.modes').select2({ width: '140px' });
    
    /*svg support check*/
    if(document.implementation.hasFeature('http://www.w3.org/TR/SVG11/feature#Image', '1.1')){
        $('html').addClass('svg');
    } else $('html').addClass('no-svg');
    
    var modal = new Modal({
        content: "<table class='table'>\n  <thead>\n    <tr>\n      <th>Action</th>\n      <th>Win / Linux</th>\n      <th>Mac</th>\n    </tr>\n  </thead>\n  <tbody>\n    <tr>\n      <td>Save</td>\n      <td><code>&lt;Ctrl + s&gt;</code></td>\n      <td> <code>&lt;Command + s&gt;</code></td>\n    </tr>\n    <tr>\n      <td>Find</td>\n      <td></td>\n      <td></td>\n    </tr>\n    <tr>\n      <td>Find Next</td>\n      <td></td>\n      <td></td>\n    </tr>\n    <tr>\n      <td>Find Previous</td>\n      <td></td>\n      <td></td>\n    </tr>\n    <tr>\n      <td>Find + Replace</td>\n      <td></td>\n      <td></td>\n    </tr>\n  </tbody>\n</table>",
        maxWidth: 500
    });
    
    $(".info-btn").click(function(){
        modal.open();
    });
    
});
