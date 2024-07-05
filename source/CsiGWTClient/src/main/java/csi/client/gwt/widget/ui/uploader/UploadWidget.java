package csi.client.gwt.widget.ui.uploader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.client.gwt.util.Display;
import csi.server.common.util.StringUtil;
import org.moxieapps.gwt.uploader.client.File;
import org.moxieapps.gwt.uploader.client.Uploader;
import org.moxieapps.gwt.uploader.client.events.FileDialogCompleteEvent;
import org.moxieapps.gwt.uploader.client.events.FileDialogCompleteHandler;
import org.moxieapps.gwt.uploader.client.events.FileDialogStartEvent;
import org.moxieapps.gwt.uploader.client.events.FileDialogStartHandler;
import org.moxieapps.gwt.uploader.client.events.FileQueueErrorEvent;
import org.moxieapps.gwt.uploader.client.events.FileQueueErrorHandler;
import org.moxieapps.gwt.uploader.client.events.FileQueuedEvent;
import org.moxieapps.gwt.uploader.client.events.FileQueuedHandler;
import org.moxieapps.gwt.uploader.client.events.UploadCompleteEvent;
import org.moxieapps.gwt.uploader.client.events.UploadCompleteHandler;
import org.moxieapps.gwt.uploader.client.events.UploadErrorEvent;
import org.moxieapps.gwt.uploader.client.events.UploadErrorHandler;
import org.moxieapps.gwt.uploader.client.events.UploadProgressEvent;
import org.moxieapps.gwt.uploader.client.events.UploadProgressHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.client.gwt.widget.buttons.CsiBrowseButton;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.service.api.UserFileActionsServiceProtocol;
import csi.shared.core.Constants;

public class UploadWidget<T extends SelectorBasics> implements IsWidget, CsiBrowseButton<T> {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Embedded Classes                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	public class UploadFile {

		private String _id;
		private String _sourceName;
		private String _targetName;
		private long _size;
		private boolean _canceled;

		public UploadFile(String idIn, String sourceNameIn, String targetNameIn, long sizeIn) {

			_id = idIn;
			_sourceName = sourceNameIn;
			_targetName = targetNameIn;
			_size = sizeIn;
			_canceled = false;
		}

		public void cancel() {

			_canceled = true;
		}

		public boolean isCanceled() {

			return _canceled;
		}

		public String getId() {

			return _id;
		}

		public String getSourceName() {

			return _sourceName;
		}

		public String getTargetName() {

			return _targetName;
		}

		public long getSize() {

			return _size;
		}
	}
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

	private Uploader _uploader;
	private FileUploadProgressDialog _progressBar = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

	protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtFailureDialogTitle = _constants.serverRequest_ErrorDialogTitle();

	private static final String DATAFILES_DIR = "datafiles"; //$NON-NLS-1$

	private static final String _txtFileUploadHeader = _constants.uploadWidget_DialogTitle();
	private static final String _txtChoiceDialogTitle = _constants.uploadWidget_Choice_DialogTitle();
	private static final String _txtChoiceInfoString = _constants.fileColisionChoice_InfoString(
            Dialog.txtOverwriteButton, Dialog.txtSkipButton, Dialog.txtCancelButton);

	private static final List<ButtonDef> _buttonList = Arrays.asList(

	new ButtonDef(Dialog.txtSkipButton), new ButtonDef(Dialog.txtOverwriteButton));

    private static final long _blockSize = 100L * 1024L * 1024L;

	private int _height = 0;
	private int _width = 0;
	private int _conflictNumber = 0;
	private int _rejectCount = 0;
	private boolean _canceled = false;
	private StringBuilder _conflictBuffer = null;

	private List<Map<String, T>> _rejectionList = null;
	private List<Integer> _conflictList = null;
	private List<String> _failedList = null;
	private List<String> _xferedList = null;
	private List<UploadFile> _fileList = null;
	private Map<String, Integer> _idMap = null;

	private HandlerManager _handlerManager;

    private boolean _useCaselessFilenames = WebMain.injector.getMainPresenter().useCaselessFilenames();
	private boolean _showComplete = false;
	private Long _maxFileSize = null;
    private long _blocksCompleted = 0L;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle response to request for overwrite resource list
    //
    public VortexEventHandler<Integer> handleMaxSizeResponse
            = new AbstractVortexEventHandler<Integer>() {
        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(_txtFailureDialogTitle, myException);

            return false;
        }
        @Override
        public void onSuccess(Integer sizeIn) {

            setMaxFileSize(new Long(sizeIn));
        }
    };

    FileDialogStartHandler handleFileDialogStart = new FileDialogStartHandler() {

		public boolean onFileDialogStartEvent(FileDialogStartEvent fileDialogStartEvent) {

			_fileList = new ArrayList<UploadFile>();
			_idMap = new HashMap<String, Integer>();
			_conflictList = new ArrayList<Integer>();
			_xferedList = new ArrayList<String>();
			_failedList = new ArrayList<String>();
			_conflictBuffer = new StringBuilder();
			_conflictNumber = 0;
			_rejectCount = 0;
			_canceled = false;
			return true;
		}
	};

	FileQueuedHandler handleQueuedFiles = new FileQueuedHandler() {

		public boolean onFileQueued(final FileQueuedEvent fileQueuedEvent) {

			File myFile = fileQueuedEvent.getFile();
			String mySourceName = StringUtil.removePath(myFile.getName());
			Integer myIndex = new Integer(_fileList.size());
			long mySize = myFile.getSize();

			_idMap.put(myFile.getId(), myIndex);
			_fileList.add(new UploadFile(myFile.getId(), mySourceName, mySourceName, mySize));

			if ((null != _maxFileSize) && (mySize > _maxFileSize)) {

				_rejectCount++;
				_failedList.add(mySourceName + i18n.uploadWidgetQueueError() //$NON-NLS-1$
						+ _maxFileSize.toString());
				cancelFile(_fileList.get(myIndex));
			} else if (CheckNameConflict(mySourceName)) {

				_conflictList.add(myIndex);
			}
			return true;
		}
	};

	FileDialogCompleteHandler handleFileDialogComplete = new FileDialogCompleteHandler() {

		public boolean onFileDialogComplete(FileDialogCompleteEvent fileDialogCompleteEvent) {

			if (0 < _fileList.size()) {

				resolveConflicts();
			}
			return true;
		}
	};

	UploadProgressHandler handleUploadProgress = new UploadProgressHandler() {

		public boolean onUploadProgress(UploadProgressEvent uploadProgressEvent) {

            long myCount = uploadProgressEvent.getBytesComplete();
            long myBlocks = myCount / _blockSize;

			if (_canceled) {

				_uploader.cancelUpload(false);

			} else if (_blocksCompleted < myBlocks) {

                String myId = uploadProgressEvent.getFile().getId();
                Integer myIndex = _idMap.get(myId);

                _blocksCompleted = myBlocks;

                if ((null != myIndex) && (0 <= myIndex) && (_fileList.size() > myIndex)) {

                    UploadFile myFile = _fileList.get(myIndex);

                    _progressBar.setFileProgress(myFile, myCount);
                }
            }

			return true;
		}
	};

	UploadCompleteHandler handleUploadComplete = new UploadCompleteHandler() {

		public boolean onUploadComplete(UploadCompleteEvent uploadCompleteEvent) {

			fileComplete(uploadCompleteEvent);
			return true;
		}

	};

	FileQueueErrorHandler handleFileQueueError = new FileQueueErrorHandler() {

		public boolean onFileQueueError(FileQueueErrorEvent fileQueueErrorEvent) {

			File myFileInfo = fileQueueErrorEvent.getFile();

			_failedList.add(myFileInfo.getName() + i18n.uploadWidgetQueuingError() + fileQueueErrorEvent.getMessage()); //$NON-NLS-1$
			return true;
		}
	};

	UploadErrorHandler handleUploadError = new UploadErrorHandler() {

		public boolean onUploadError(UploadErrorEvent uploadErrorEvent) {

			Display.error(i18n.uploadWidgetUploadError() + uploadErrorEvent.getErrorCode(), uploadErrorEvent.getMessage()); //$NON-NLS-1$
			return true;
		}
	};

	ClickHandler handleCancelRequest = new ClickHandler() {

		@Override
		public void onClick(ClickEvent eventIn) {

			cancelUpload();
		}
	};

	//
	// Handle choice being made when file names collide
	//
	protected ChoiceMadeEventHandler handleChoiceMadeEvent = new ChoiceMadeEventHandler() {
		@Override
		public void onChoiceMade(ChoiceMadeEvent eventIn) {

			switch (eventIn.getChoice()) {

			case 0:

				//
				// Cancel entire upload
				//
				cancelUpload();
				break;

			case 1:

				//
				// Skip current file
				//
				if (_conflictList.size() > _conflictNumber) {

					cancelFile(_fileList.get(_conflictList.get(_conflictNumber)));
					_conflictNumber++;
				}
				break;

			case 2:

				//
				// Overwrite existing file -- no action required
				//
				_conflictNumber++;
				break;
			}
			resolveConflicts();
		}
	};


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
	public static String formatFileTypes(List<String> listIn) {

		StringBuilder myBuffer = new StringBuilder();

		if ((null != listIn) && (0 < listIn.size())) {

			for (String myExtension : listIn) {

				int myIndex = myExtension.lastIndexOf('.');

				if (-1 == myIndex) {

					myBuffer.append(";*.").append(myExtension); //$NON-NLS-1$

				} else if ((myExtension.length() - 1) > myIndex) {

					myBuffer.append(";*.").append(myExtension.substring(myIndex + 1)); //$NON-NLS-1$
				}
			}
		}

		return (0 < myBuffer.length()) ? myBuffer.toString().substring(1) : null;
	}

	public UploadWidget(String labelIn, boolean allowMultipleIn) {

		_handlerManager = new HandlerManager(this);

		createObjects(labelIn, allowMultipleIn);
		wireInHandlers();
		_uploader.setButtonDisabled(true);
        requestSizeLimit();
	}

	public UploadWidget(String labelIn) {

		this(labelIn, false);
	}

	public void setMaxFileSize(Long sizeIn) {

		if (null != sizeIn) {

			// Do not allow the transfer program to block the file.
			// It will not notify us if it is the only file.
			// _uploader.setFileSizeLimit(sizeIn.toString() + "MB");

			_maxFileSize = sizeIn * 1024L * 1024L;
		}
	}

	public HandlerRegistration addTransferCompleteEventHandler(TransferCompleteEventHandler handler) {
		return _handlerManager.addHandler(TransferCompleteEvent.type, handler);
	}

	public void fireEvent(GwtEvent<?> eventIn) {
		_handlerManager.fireEvent(eventIn);
	}

	public void initialize(List<Map<String, T>> rejectionListIn, String fileTypesIn) {

        if (_useCaselessFilenames && (null != rejectionListIn) && (0 < rejectionListIn.size())) {

            Map<String, T> myNewMap = new HashMap<String, T>();

            _rejectionList = new ArrayList<Map<String, T>>();
            _rejectionList.add(myNewMap);

            for (Map<String, T> myMap : rejectionListIn) {

                for (T myConflict : myMap.values()) {

                    myNewMap.put(myConflict.getName().toLowerCase(), myConflict);
                }
            }

        } else {

            _rejectionList = rejectionListIn;
        }
		_uploader.setButtonDisabled(false);

		if (null != fileTypesIn) {

			_uploader.setFileTypes(fileTypesIn);
		}
	}

	public void setEnabled(boolean value) {
		_uploader.setButtonDisabled(!value);
	}

	@Override
	public Widget asWidget() {

		return _uploader;
	}

	public void setPixelSize(int widthIn, int heightIn) {

		_width = widthIn;
		_height = heightIn;

		if (null != _uploader) {

			_uploader.setButtonWidth(_width);
			_uploader.setButtonHeight(_height);
		}
	}

	public List<UploadFile> getUploadedFiles() {
		return _fileList;
	}

	public void showSuccessPopup(boolean showIn) {

		_showComplete = showIn;
	}


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
     
 	private void createObjects(String labelIn, boolean allowMultipleIn) {

		_uploader = new Uploader();

		_uploader.setUploadURL(GWT.getModuleBaseURL() + "upload?p=" + DATAFILES_DIR); //$NON-NLS-1$
		_uploader.setButtonText("<a class=\"btn\">" + labelIn + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		_uploader.setButtonCursor(Uploader.Cursor.HAND);
		_uploader.setButtonAction(allowMultipleIn ? Uploader.ButtonAction.SELECT_FILES
                : Uploader.ButtonAction.SELECT_FILE);
	}

	private void wireInHandlers() {

		_uploader.setFileQueuedHandler(handleQueuedFiles);
		_uploader.setUploadProgressHandler(handleUploadProgress);
		_uploader.setUploadCompleteHandler(handleUploadComplete);
		_uploader.setFileDialogStartHandler(handleFileDialogStart);
		_uploader.setFileDialogCompleteHandler(handleFileDialogComplete);
		_uploader.setFileQueueErrorHandler(handleFileQueueError);
		_uploader.setUploadErrorHandler(handleUploadError);
	}

	private void beginUploading(int fileCountIn) {

		JSONObject json = new JSONObject();
		json.put(Constants.FileConstants.UPLOAD_FILE_MAPPING_PART, new JSONString(_conflictBuffer.toString()));
		_uploader.setPostParams(json);

		_progressBar = new FileUploadProgressDialog();
		_progressBar.addCancelHandler(handleCancelRequest);
		_progressBar.show(fileCountIn);

		uploadFile(0);
	}

	private void uploadFile(int nextItemIn) {

		_uploader.startUpload();
	}

	private boolean CheckNameConflict(String nameIn) {

		boolean myConflict = false;
		String myTest = _useCaselessFilenames ? nameIn.toLowerCase() : nameIn;

		if ((null != _rejectionList) && (0 < _rejectionList.size())) {

			for (Map<String, T> myMap : _rejectionList) {

				if ((null != myMap) && myMap.containsKey(myTest)) {

					myConflict = true;
					break;
				}
			}
		}

		return myConflict;
	}

	private void fileComplete(UploadCompleteEvent uploadCompleteEvent) {

		File myFileInfo = uploadCompleteEvent.getFile();
		String myId = myFileInfo.getId();
		File.Status myStatus = myFileInfo.getStatus();

		if (null != myId) {

			Integer myItemIndex = _idMap.get(myId);

			if (null != myItemIndex) {

				switch (myStatus) {

				case ERROR:

					_failedList.add(myFileInfo.getName() + i18n.uploadWidgetFailedUploading()); //$NON-NLS-1$
					break;

				case CANCELLED:

					_failedList.add(myFileInfo.getName() + i18n.uploadWidgetCanceled()); //$NON-NLS-1$
					break;

				case QUEUED:
				case IN_PROGRESS:

					break;

				case COMPLETE:

					_xferedList.add(uploadCompleteEvent.getFile().getName());
					break;

				case UNKNOWN:

					break;
				}

				myItemIndex++;

				_progressBar.setLoadComplete(myItemIndex);

				if ((_fileList.size() > myItemIndex) && (!_canceled)) {
					uploadFile(myItemIndex);
				} else {

					displayResults();
					shutdown();
				}
			}
		}
	}

	private void displayResults() {

		if (_showComplete && (0 == _failedList.size())) {

			Dialog.showSuccess(_txtFileUploadHeader, i18n.uploadWidgetCompleted()); //$NON-NLS-1$

		} else if (0 < _failedList.size()) {

			StringBuilder myMessage = new StringBuilder();

			myMessage.append(i18n.uploadWidgetNotUploadedCorrectly()); //$NON-NLS-1$
			for (String myItem : _failedList) {

				myMessage.append('\n');
				myMessage.append(myItem);
			}
			Display.error(i18n.uploadWidgetErrorMessage(), myMessage.toString()); //$NON-NLS-1$
		}
	}

	private void shutdown() {

		fireEvent(new TransferCompleteEvent(_xferedList));
		_canceled = false;
		hideProgressBar();
	}

	private void resolveConflicts() {

		if (!_canceled) {

			if (_conflictList.size() > _conflictNumber) {

				UploadFile myFile = _fileList.get(_conflictList.get(_conflictNumber));

				(new DecisionDialog(_txtChoiceDialogTitle, _constants.fileColision_InfoString(myFile.getSourceName())
						+ _txtChoiceInfoString, _buttonList, handleChoiceMadeEvent, 70)).show();

			} else if (_fileList.size() > _rejectCount) {

				beginUploading(_fileList.size() - _rejectCount);

			} else {

				displayResults();
			}
		}
	}

	private void cancelUpload() {

		_canceled = true;

		for (UploadFile myFile : _fileList) {

			cancelFile(myFile);
		}
	}

	private void cancelFile(UploadFile fileIn) {

		if ((null != fileIn) && (!fileIn.isCanceled())) {

			_uploader.cancelUpload(fileIn.getId(), false);
			fileIn.cancel();
			_rejectCount++;
		}
	}

	private void hideProgressBar() {

		if (null != _progressBar) {

			_progressBar.hide();
		}
	}

    private void requestSizeLimit() {

        VortexFuture<Integer> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            myVortexFuture.execute(UserFileActionsServiceProtocol.class).getMaxUploadSize();
            myVortexFuture.addEventHandler(handleMaxSizeResponse);

        } catch (Exception myException) {

            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }
}
