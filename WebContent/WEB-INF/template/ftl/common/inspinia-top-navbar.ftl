        			
        			<nav class="navbar navbar-static-top" role="navigation">
		            <div class="navbar-header">
		                <button aria-controls="navbar" aria-expanded="false" data-target="#navbar" data-toggle="collapse" class="navbar-toggle collapsed" type="button">
		                    <i class="fa fa-reorder"></i>
		                </button>
		                <a href="#" class="navbar-brand">MUSI</a>
		            </div>
		            <div class="navbar-collapse collapse" id="navbar">
		                <ul class="nav navbar-nav">
		                    <li class="active">
		                        <a aria-expanded="false" role="button" href="layouts.html"> Back to main Layout page</a>
		                    </li>
		                    <li class="dropdown">
		                        <a aria-expanded="false" role="button" href="#" class="dropdown-toggle" data-toggle="dropdown"> Menu item <span class="caret"></span></a>
		                        <ul role="menu" class="dropdown-menu">
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                        </ul>
		                    </li>
		                    <li class="dropdown">
		                        <a aria-expanded="false" role="button" href="#" class="dropdown-toggle" data-toggle="dropdown"> Menu item <span class="caret"></span></a>
		                        <ul role="menu" class="dropdown-menu">
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                        </ul>
		                    </li>
		                    <li class="dropdown">
		                        <a aria-expanded="false" role="button" href="#" class="dropdown-toggle" data-toggle="dropdown"> Menu item <span class="caret"></span></a>
		                        <ul role="menu" class="dropdown-menu">
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                        </ul>
		                    </li>
		                    <li class="dropdown">
		                        <a aria-expanded="false" role="button" href="#" class="dropdown-toggle" data-toggle="dropdown"> Menu item <span class="caret"></span></a>
		                        <ul role="menu" class="dropdown-menu">
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                            <li><a href="">Menu item</a></li>
		                        </ul>
		                    </li>
		
		                </ul>
		                <ul class="nav navbar-top-links navbar-right">
		                    <li>
		                    		<#if user.anonymous >
		                        <a href="/accounts/login">
		                            <i class="fa fa-sign-out"></i> 로그인
		                        </a>
		                        <#else>
		                        <a href="/accounts/logout">
		                            <i class="fa fa-sign-out"></i> 로그아웃
		                        </a>		                        
		                        </#if>
		                    </li>
		                </ul>
		            </div>
        			</nav>
        		</div>