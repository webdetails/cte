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
  notify: function(msg, type) { 
    // console.log(type + " : " + msg); 
  }
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

      if (window.history.pushState) {
       window.history.pushState( "", "" , "" + buildUrl( loadedFileName, loadedFileProvider, true ) );
      }
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
    
    var fileNameIndex = fileName.lastIndexOf("/") + 1;
    var file = fileName.substr(fileName.lastIndexOf("/") + 1);
    var pathname = fileName.substr(0, fileNameIndex);
    
    //$infoArea.html("(...)" + _path.substring(_path.indexOf("/"), _path.length));
    $infoArea.html("<span>(...)/</span>" + "<span class='bold'>"+file+"</span>");
  } else {
    //$infoArea.html(fileName);
    var fileNameIndex = fileName.lastIndexOf("/") + 1;
    var file = fileName.substr(fileName.lastIndexOf("/") + 1);
    var pathname = fileName.substr(0, fileNameIndex);
    
    $infoArea.html("<span>" + pathname + "</span>" + "<span class='bold'>" + file);
  }

  if(typeof(listeners.onStatusUpdate) == 'function') {
    listeners.onStatusUpdate(isDirty);
  }
};

var buildUrl = function( filePath, fileProvider, encode ) {
  var url = window.location.origin + window.location.pathname;

  if( filePath ) {
    url += "?path=" + ( encode ? encodeURIComponent( filePath ) : filePath );
  
    // no sense in having a provider param if we don't have a file param
    if( fileProvider ) {
      url += "&provider=" + ( encode ? encodeURIComponent( fileProvider ) : fileProvider ); 
    }
  } 

  return url;
}

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
  
  $('#fileTreeAccordion').height(window.innerHeight - (params.editorOnly ? 20 : 100));
  $('#editArea').height(window.innerHeight - (params.editorOnly ? 20 : 100));
  
  $('#fileTreeAccordion').width(window.innerWidth - (window.innerWidth * 0.75) - 25);
  $('#editArea').width('75%');
      
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
    // custom confirm/discard dialog
    var confirmDiscard = new Modal({
      header: true, 
      content: "You have unsaved changes. </br> Do you wish to save them or proceed without saving?", 
      confirmDialog: true, 
      saveBtn: true, 
      dontSaveBtn: true,
      cancelBtn: true 
    });

    var loadAnother = function( f , p ){
      $(".selectedFile").attr("class", "");
      $("a[rel='" + f + "']").attr("class", "selectedFile");
      load("/" + f , p );
    };

  editor.getProviders( function f( providers ){
    if( providers && providers.length > 0 ){
      for( i = 0; i < providers.length; i++ ){
        var providerId = providers[i].id;
        var providerName = providers[i].name;

        var $divTreePanel = $('<div class="panel panel-default">')
          .append('<div class="panel-heading" provider-id="' + providerId + '"><h4 class="panel-title"><a data-toggle="collapse" data-parent="#fileTreeAccordion" href="#panel-' + providerId + '" >' + providerName + '</a></h4><h4 class="refresh-provider" provider-id="' + providerId + '" provider-name="' + providerName + '" /></div>');
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

          if( isDirty ){

            confirmDiscard.open();

            // 'save' button already has an attached event listener that handles the file saving
            $('div.ml-confirm-dialog > button.save').click( function ( data, fn ){ loadAnother( file, provider ) });

            $('div.ml-confirm-dialog > button.no-save').click( function ( data, fn ){ loadAnother( file, provider ) });

          } else {

            loadAnother( file, provider )
          }
          
        });

        $divTreeCollapsible.append($('<div class="panel-body">').append($divTree));
        $divTreePanel.append($divTreeCollapsible);
        $('#fileTreeAccordion').append($divTreePanel);
        
        $('.panel-title a').addClass('collapsed');
      }
    }
  });

  $('#fileTreeAccordion h4.refresh-provider').on( 'click', function(){
    var providerId = $(this).attr('provider-id');
    var providerName = $(this).attr('provider-name');
    if( providerId ){
      $('div#treeDiv-' + providerId ).fileTree({
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

          if( isDirty ){

            confirmDiscard.open();

            // 'save' button already has an attached event listener that handles the file saving
            $('div.ml-confirm-dialog > button.save').click( function ( data, fn ){ loadAnother( file, provider ) });

            $('div.ml-confirm-dialog > button.no-save').click( function ( data, fn ){ loadAnother( file, provider ) });

          } else {

            loadAnother( file, provider )
          }
          
      });
    }
  });


  $('#fileTreeAccordion h4.panel-title > a').on( 'click', function(){
    
    // first, hide all refresh icons
    $('#fileTreeAccordion h4.refresh-provider').each( function(){ $(this).css('display', 'none' ); });  

    // determine if this is an action of a provider that is now being expanded
    var providerWasCollapsed = $(this).attr('class') == 'collapsed';
    var refreshIconDisplayVal = providerWasCollapsed ? 'block' : 'none'; // new refresh icon display value

    // update this provider's refresh icon display value accordingly
    $(this).closest('div.panel-heading').find('h4.refresh-provider').css('display', refreshIconDisplayVal );
  });

  $('#providers-collapser-column').on( 'click', function(){
    
    // determine the current status of the providerListColumn ( collapsed / expanded )
    var providersListColumnIsCollapsed = $('td#providers-list-column').css('display') == 'none';

    var providersListColumnNewVal = providersListColumnIsCollapsed ? 'table-cell' /* default */ : 'none';
    var collapserOldArrow = providersListColumnIsCollapsed ? 'arrow-right.png' /* default */ : 'arrow-left.png';
    var collapserNewArrow = providersListColumnIsCollapsed ? 'arrow-left.png' /* default */ : 'arrow-right.png';
    
    $('tr#header-row td.col1').css('display', providersListColumnNewVal );
    $('tr#content-row td.col1').css('display', providersListColumnNewVal );
    
    var imgSrc = $('tr#content-row img#collapser-img').attr('src' );
    $('tr#content-row img#collapser-img').attr('src' , imgSrc.replace( collapserOldArrow, collapserNewArrow ) );

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

$(window).resize(function() {
  $('#fileTreeAccordion').height(window.innerHeight - (params.editorOnly ? 20 : 100));
  $('#editArea').height(window.innerHeight - (params.editorOnly ? 20 : 100));

  $('#fileTreeAccordion').width(250);
  $('#editArea').width(window.innerWidth - 311);
});


$(function() {
  $(".modes").select2({width: '120px'});
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
    
    var shortcutsModal = new Modal({
        content: "<div class='popup-keys-table'>\n<table class='table'>\n<thead>\n<tr>\n<th>Action</th>\n<th>PC ( Win/Linux )</th>\n<th>Mac</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>Save</td>\n<td><code>Ctrl + S</code></td>\n<td> <code>Command + S</code></td>\n</tr>\n<tr>\n<td>Find</td>\n<td><code>Ctrl + F</code></td>\n<td><code>Command + F</code></td>\n</tr>\n<tr>\n<td>Find Next</td>\n<td><code>Ctrl + K</code></td>\n<td><code>Command + G</code></td>\n</tr>\n<tr>\n<td>Find Previous</td>\n<td><code>Ctrl + Shift + K</code></td>\n<td><code>Command + Shift + G</code></td>\n</tr>\n<tr>\n</tbody>\n</table>\n</div>\n<div>\n<span class='popup-keys-label'>For a complete list of shortcuts please refer to this <a href='https://github.com/ajaxorg/ace/wiki/Default-Keyboard-Shortcuts' target='_blank'>link</a> </span>\n</div>",
        maxWidth: 500,
        header: true
    });
    
    $(".info-btn").click(function(){
        shortcutsModal.open({closeButton: true});
    });
    

    
});