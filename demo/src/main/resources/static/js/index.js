$(document).ready(function(){
	
	$("#simpleUpload").click(function(){
		$.ajax({
			url: '/create',
			success: function(){
				alert("File upload complete.");
			}
		});
	});
	
	$("#refreshFileButton").click(function(){
		$.ajax({
			url: '/list',
			success: function(){
				alert("File list complete.");
			}
		}).done(function(data){
			console.log(data);
			var fileHTML = "";

			data.forEach(function(item, idx, arr){
					fileHTML += '<li class="list-group-item" mime-type="' + arr[idx].mimetype + '" file-kind="' + arr[idx].kind + '">';
					fileHTML += '<a href="' + arr[idx].webViewLink + '">';
					if(arr[idx].mimetype === 'application/vnd.google-apps.folder'){ 
						fileHTML += '<i class="fas fa-folder"></i> ';
					}
					if(arr[idx].isShared){
						fileHTML += '[공유] ';
					}
					fileHTML += arr[idx].name; 
					fileHTML += '</a></li>';
			});

			$("#fileListcontainer").html(fileHTML);
		});
	});
});