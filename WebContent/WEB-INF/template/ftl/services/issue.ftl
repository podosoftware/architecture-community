<#ftl encoding="UTF-8"/>
<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <title><#if __page?? >${__page.title}</#if></title>
 	
 	<!-- Kendoui with bootstrap theme CSS -->			 
	<link href="<@spring.url "/css/kendo.ui.core/web/kendo.common-bootstrap.core.min.css"/>" rel="stylesheet" type="text/css" />	
	<link href="<@spring.url "/css/kendo.ui.core/web/kendo.bootstrap.min.css"/>" rel="stylesheet" type="text/css" />	 
	
	<!-- Bootstrap core CSS -->
	<link href="<@spring.url "/css/bootstrap/3.3.7/bootstrap.min.css"/>" rel="stylesheet" type="text/css" />
	<link href="<@spring.url "/fonts/font-awesome.css"/>" rel="stylesheet" type="text/css" />	

	<!-- Bootstrap Theme CSS -->
	<link href="<@spring.url "/css/bootstrap.theme/inspinia/style.css"/>" rel="stylesheet" type="text/css" />	
 	<link href="<@spring.url "/css/bootstrap.theme/inspinia/custom.css"/>" rel="stylesheet" type="text/css" />	
		
	<!-- Community CSS -->
	<link href="<@spring.url "/css/community.ui/community.ui.globals.css"/>" rel="stylesheet" type="text/css" />	
	<link href="<@spring.url "/css/community.ui/community.ui.components.css"/>" rel="stylesheet" type="text/css" />
  	<link href="<@spring.url "/css/community.ui/community.ui.style.css"/>" rel="stylesheet" type="text/css" />	
  	
  	 <!-- Summernote Editor CSS -->
	<link href="<@spring.url "/js/summernote/summernote.css"/>" rel="stylesheet" type="text/css" />
		
	<!-- Page landing js -->	   	
	<script data-pace-options='{ "ajax": false }' src='<@spring.url "/js/pace/pace.min.js"/>'></script>   	
	<!-- Requirejs for js loading -->
	<script src="<@spring.url "/js/require.js/2.3.5/require.js"/>" type="text/javascript"></script>
	<!-- Application JavaScript
    		================================================== -->    
	<script>
	
	var __projectId = <#if RequestParameters.projectId?? >${RequestParameters.projectId}<#else>0</#if>;
	
	require.config({
		shim : {
	        "bootstrap" : { "deps" :['jquery'] },
	        "kendo.ui.core.min" : { "deps" :['jquery'] },
	        "kendo.culture.ko-KR.min" : { "deps" :['kendo.ui.core.min'] },
	        "community.ui.core" : { "deps" :['kendo.culture.ko-KR.min'] },
	        "community.data" : { "deps" :['community.ui.core'] },	        
	        "summernote-ko-KR" : { "deps" :['summernote.min'] }
	    },
		paths : {
			"jquery"    					: "/js/jquery/jquery-2.2.4.min",
			"bootstrap" 					: "/js/bootstrap/3.3.7/bootstrap.min",
			"kendo.ui.core.min" 			: "/js/kendo.ui.core/kendo.ui.core.min",
			"kendo.culture.ko-KR.min"	: "/js/kendo.ui.core/cultures/kendo.culture.ko-KR.min",
			"community.ui.core" 			: "/js/community.ui/community.ui.core",
			"community.data" 			: "/js/community.ui/community.data",
			"summernote.min"             : "/js/summernote/summernote.min",
			"summernote-ko-KR"           : "/js/summernote/lang/summernote-ko-KR"		
		}
	});
	
	require([ "jquery", "kendo.ui.core.min",  "kendo.culture.ko-KR.min", "community.data", "community.ui.core", "bootstrap"], function($, kendo ) {	
		community.ui.setup({
		  	features : {
				accounts: true
		  	},
		  	'features.accounts.authenticate' :function(e){
		  		if( !e.token.anonymous ){
		  			observable.setUser(e.token);
		    		}
		  	}
		});	        
		
        // Topnav animation feature
 		var cbpAnimatedHeader = (function() {
        		var docElem = document.documentElement, header = document.querySelector( '.navbar-default' ), didScroll = false, changeHeaderOn = 200;
        		function init() {
            		window.addEventListener( 'scroll', function( event ) {
	                if( !didScroll ) {
	                    didScroll = true;
	                    setTimeout( scrollPage, 250 );
	                }
            		}, false );
        		}
        		function scrollPage() {
            		var sy = scrollY();
	            if ( sy >= changeHeaderOn ) {
	                $(header).addClass('navbar-scroll')
	            }
	            else {
	                $(header).removeClass('navbar-scroll')
	            }
            		didScroll = false;
        		}
	        function scrollY() {
	            return window.pageYOffset || docElem.scrollTop;
	        }
        		init();
		})();
           		
		var observable = new community.ui.observable({ 
			currentUser : new community.model.User(),
			projectId : __projectId,
			project : new community.model.Project(),
			projecPeriod : "",
			setUser : function( data ){
				var $this = this;
				data.copy($this.currentUser)
			},
			loadProjectInfo : function( ){
				var $this = this;
				community.ui.ajax('/data/api/v1/projects/'+ observable.get('projectId') +'/info.json/', {
					success: function(data){		
						$this.set('project', new community.model.Project(data) );		
						$this.set('projecPeriod' , community.data.getFormattedDate( $this.project.startDate , 'yyyy-MM-dd')  +' ~ '+  community.data.getFormattedDate( $this.project.endDate, 'yyyy-MM-dd' ) );
					}	
				});
			}
    		});
		observable.loadProjectInfo();
		createIssueListView(observable);		
		var renderTo = $('#page-top');
		renderTo.data('model', observable);		
		community.ui.bind(renderTo, observable );	
		renderTo.on("click", "button[data-action=create], a[data-action=create]", function(e){			
			var $this = $(this);
			var actionType = $this.data("action");		
			var objectId = $this.data("object-id");		
			var targetObject = new community.model.Issue();	
			targetObject.set('objectType', 19);
			targetObject.set('objectId', __projectId);
 			createOrOpenIssueEditor (targetObject);
			return false;		
		});
	});
	
	function createIssueListView( observable ){
		var renderTo = $('#issue-listview');
		community.ui.listview( renderTo , {
			dataSource: community.ui.datasource('<@spring.url "/data/api/v1/projects/"/>'+ observable.get('projectId') +'/issues/list.json', {
				schema: {
					total: "totalCount",
					data: "items",
					model : community.model.Issue
				}
			}),
			template: community.ui.template($("#template").html())
		});	
	}
 
 
	function createOrOpenIssueEditor( data ){ 
		var renderTo = $('#issue-editor-modal');
		if( !renderTo.data("model") ){
			var observable = new community.ui.observable({ 
				isNew : false,	
				isDeveloper : false,
				issue : new community.model.Issue(),
				issueTypeDataSource : community.ui.datasource( '<@spring.url "/data/api/v1/codeset/ISSUE_TYPE/list.json" />' , {} ),
				priorityDataSource  : community.ui.datasource( '<@spring.url "/data/api/v1/codeset/PRIORITY/list.json" />' , {} ),
			 	methodsDataSource   : community.ui.datasource( '<@spring.url "/data/api/v1/codeset/SUPPORT_METHOD/list.json" />' , {} ),
			 	resolutionDataSource : community.ui.datasource( '<@spring.url "/data/api/v1/codeset/RESOLUTION/list.json" />' , {} ),
			 	statusDataSource : community.ui.datasource( '<@spring.url "/data/api/v1/codeset/ISSUE_STATUS/list.json" />' , {} ),
			 	setSource : function( data ){
			 		var $this = this;
					var orgIssueId = $this.issue.issueId ;
					data.copy( $this.issue ); 
					if(  $this.issue.issueId > 0 ){
						$this.set('isNew', false );
					}else{
						$this.set('isNew', true );	
					}
					$this.set('isDeveloper', isDeveloper());
			 	},
				saveOrUpdate : function(e){				
					var $this = this;
					community.ui.progress(renderTo.find('.modal-content'), true);	
					community.ui.ajax( '<@spring.url "/data/api/v1/issues/save-or-update.json" />', {
						data: community.ui.stringify($this.issue),
						contentType : "application/json",						
						success : function(response){
							
						}
					}).always( function () {
						community.ui.progress(renderTo.find('.modal-content'), false);
						renderTo.modal('hide');
					});						
				}
			});
			renderTo.data("model", observable );	
			community.ui.bind( renderTo, observable );				
			renderTo.on('show.bs.modal', function (e) {	});
		}	
		
		if( community.ui.defined(data) ) 
			renderTo.data("model").setSource(data);
		
		renderTo.modal('show');
	}

	function isDeveloper(){ 
		return $('#page-top').data('model').currentUser.hasRole('ROLE_DEVELOPER') ;
	} 
 			
	</script>		
</head>
<body id="page-top" class="landing-page no-skin-config">
	<!-- NAVBAR START -->   
	<#include "/includes/user-top-navbar.ftl">
	<!-- NAVBAR END -->   
	<section class="u-bg-overlay g-bg-cover g-bg-size-cover g-bg-bluegray-opacity-0_3--after" style="background: url(https://htmlstream.com/preview/unify-v2.4/assets/img-temp/1920x800/img8.jpg)">      
      <div class="container text-center g-bg-cover__inner g-py-50">
        <div class="row justify-content-center">
          <div class="col-lg-6">
            <div class="mb-5">
              <h1 class="g-color-darkgray g-font-size-60 mb-4"><#if __page?? >${__page.title}</#if></h1>
              <!--<h2 class="g-color-darkgray g-font-weight-400 g-font-size-22 mb-0 text-left" style="line-height: 1.8;"><#if __page?? >${__page.summary}</#if></h2>-->
            </div>
            <!-- Promo Blocks - Input -->
			<p>
				<a class="btn btn-lg u-btn-blue g-mr-10 g-mt-25" href="#" role="button" data-object-id="0" data-action="create" data-action-target="issue">기술지원요청하기</a>
			</p>            
            <!-- End Promo Blocks - Input -->
          </div>
        </div>
      </div>
    </section>

	<section id="features" class="container services">
		<div class="wrapper wrapper-content">
            		<div class="container">            
                		<div class="row">
                    		<div class="col-lg-12">
                        		<div class="ibox float-e-margins">
                            		<div class="ibox-title">
                                		<h2>
                                			<span data-bind="text:project.name"></span>
                                			<div class="g-pt-15 g-pb-15 g-font-size-20 g-font-weight-200" data-bind="html: project.summary"></div>
                                			<div class="g-font-size-20 g-font-weight-200"><span class="text-warning" data-bind="text: projecPeriod "/></div>
                                		</h5>
                                		
                            		</div>
	                            <div class="ibox-content">

 
              <!--Issue ListView-->
              <div class="table-responsive">
                <table class="table table-bordered u-table--v2">
                  <thead class="text-uppercase g-letter-spacing-1">
                    <tr>
                    	  <th class="align-middle g-font-weight-300 g-color-black g-min-width-40">ID</th>	
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-300">요약</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-65">유형</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-40">중요도</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-40">보고자</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-40">담당자</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-40">상태</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-40">결과</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-70 text-nowrap">예정일</th>
                      <th class="align-middle g-font-weight-300 g-color-black g-min-width-70 text-nowrap">생성일</th>
                    </tr>
                  </thead>

                  <tbody id="issue-listview" >
                    
                  </tbody>
                </table>
              </div>
              <!--End Issue ListView -->
	                            </div>
                        		</div>
                    		</div>
                		</div>
            		</div>
        		</div>
	</section>


	
	<!-- issue editor modal -->
	<div class="modal fade" id="issue-editor-modal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog modal-lg" role="document">
			<div class="modal-content">
				<div class="modal-header">
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
			          	<i aria-hidden="true" class="icon-svg icon-svg-sm icon-svg-ios-close m-t-xs"></i>
			        </button>
			        <h2 class="modal-title">기술지원요청</h2>
		      	</div><!-- /.modal-content -->
				<div class="modal-body">				
				 <form>
				 	<h6 class="text-light-gray text-semibold">요청구분 <span class="text-danger">*</span></h6>
					<div class="form-group">
						<input data-role="dropdownlist"  
						   data-placeholder="선택"
		                   data-auto-bind="true"
		                   data-value-primitive="true"
		                   data-text-field="name"
		                   data-value-field="code"
		                   data-bind="value:issue.issueType, source:issueTypeDataSource"
		                   style="width:100%;"/>	
		                   		        	  
					</div>	
					<h6 class="text-light-gray text-semibold">요약 <span class="text-danger">*</span></h6>				 	
				 	<div class="form-group">
			            <input type="text" class="form-control" placeholder="요약" data-bind="value: issue.summary">
			        </div> 	

					<h6 class="text-light-gray text-semibold">상세 내용 <span class="text-danger">*</span></h6>				 	
				 	<div class="form-group">
					<textarea class="form-control" placeholder="상세내용" data-bind="value:issue.description"></textarea>
	 				</div> 
					<h6 class="text-light-gray text-semibold">우선순위 <span class="text-danger">*</span></h6>
					<div class="form-group">
						<input data-role="dropdownlist"  
						   data-placeholder="선택"
		                   data-auto-bind="true"
		                   data-value-primitive="true"
		                   data-text-field="name"
		                   data-value-field="code"
		                   data-bind="value:issue.priority, source:priorityDataSource"
		                   style="width: 100%;"/>			        	  
					</div>
						
					<h6 class="text-light-gray text-semibold">지원방법</h6>
					<div class="form-group">
						<input data-role="dropdownlist"  
						   data-placeholder="선택"
		                   data-auto-bind="true"
		                   data-value-primitive="true"
		                   data-text-field="name"
		                   data-value-field="code"
		                   data-bind="source:methodsDataSource"
		                   style="width: 100%;"/>
					</div>	

					<h6 class="text-light-gray text-semibold">처리결과</h6>
					<div class="form-group">
						<input data-role="dropdownlist"  
						   data-placeholder="선택"
		                   data-auto-bind="true"
		                   data-value-primitive="true"
		                   data-text-field="name"
		                   data-value-field="code"
		                   data-bind="visible: isDeveloper,value:issue.resolution, source:resolutionDataSource"
		                   style="width: 100%;"/>
					</div>	
					
					<h6 class="text-light-gray text-semibold">상태</h6>
					<div class="form-group">
						<input data-role="dropdownlist"  
						   data-placeholder="선택"
		                   data-auto-bind="true"
		                   data-value-primitive="true"
		                   data-text-field="name"
		                   data-value-field="code"
		                   data-bind="visible: isDeveloper,value:issue.status, source:statusDataSource"
		                   style="width: 100%;"/>
					</div>	
																																						
				</form>   
				<div class="text-editor"></div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">취소</button>
					<button type="button" class="btn btn-primary" data-bind="click:saveOrUpdate">확인</button>
				</div>
			</div>
		</div>
	</div>	 
			
			
	<script type="text/x-kendo-template" id="template">
                    <tr>
                      <td class="align-middle text-center">
                      #: issueId # 	
                      </td>
                      <td class="align-middle">
                      #: summary # 	
                      </td>
                      <td class="align-middle">#: issueTypeName #</td>
                      <td class="align-middle">#: priorityName #</td>
                      <td class="align-middle">
                      	#if ( repoter.userId > 0 ) {#
                      	#= community.data.getUserDisplayName( repoter ) #
                      	#} else {#
                      		
                      	#}#
                      </td>
                      <td class="align-middle">
                      	#if ( assignee.userId > 0 ) {#
                      	#= community.data.getUserDisplayName( assignee ) #
                      	#} else {#
                      	미지정
                      	#}#
                      </td>
                      <td class="align-middle"></td>
                      <td class="align-middle"></td>
                      <td class="align-middle">#: community.data.getFormattedDate( dueDate , 'yyyy-MM-dd') #</td>
                      <td class="align-middle">#: community.data.getFormattedDate( creationDate , 'yyyy-MM-dd') #</td>
                    </tr>
	</script>	   
	 
</body>
</html>
</#compress>
