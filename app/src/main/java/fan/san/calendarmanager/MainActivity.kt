package fan.san.calendarmanager

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import fan.san.calendarmanager.ui.theme.CalendarManagerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
	@SuppressLint("CoroutineCreationDuringComposition")
	@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			val viewModel by viewModels<MainViewModel>()
			val permissionState = rememberMultiplePermissionsState(
				permissions = listOf(
					Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR
				)
			)

			val snackBarState = remember {
				SnackbarHostState()
			}

			val scope = rememberCoroutineScope()

			val context = LocalContext.current

			val msgContent = viewModel.eventFlow.collectAsState(initial = "")

			CalendarManagerTheme {
				Surface(
					modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
				) {

					Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
						CenterAlignedTopAppBar(
							title = { Text(text = "calendar account manager") },
							colors = TopAppBarDefaults.topAppBarColors(
								containerColor = MaterialTheme.colorScheme.surfaceContainer
							)
						)
					}, floatingActionButtonPosition = FabPosition.End, floatingActionButton = {
						ElevatedButton(onClick = { viewModel.createTestCalendarAccount(context) }) {
							Text(text = "创建一个测试账户")
						}
					}, snackbarHost = { SnackbarHost(hostState = snackBarState) }) {

						if (permissionState.allPermissionsGranted) {
							LocalCalendarAccount(
								modifier = Modifier.padding(top = it.calculateTopPadding()),
								viewModel = viewModel
							)
						} else {
							NoPermissionPage {
								permissionState.launchMultiplePermissionRequest()
							}
						}

						if (msgContent.value.isNotEmpty()) {
							scope.launch {
								snackBarState.showSnackbar(msgContent.value)
							}
							if (msgContent.value != "error")
								viewModel.getCalendarAccountInfo(context)
						}

						when (viewModel.deleteState) {
							is DeleteState.DeleteInfo -> {
								DialogWrapper {
									DeleteDialog(calendarName = (viewModel.deleteState as DeleteState.DeleteInfo).name,
									             id = (viewModel.deleteState as DeleteState.DeleteInfo).id,
									             confirm = { id ->
										             viewModel.removeAccount(context, id)
										             viewModel.deleteState = DeleteState.None
									             },
									             cancel = {
										             viewModel.deleteState = DeleteState.None
									             })
								}
							}

							DeleteState.None -> {

							}
						}
					}

				}
			}
		}
	}
}

@Composable
fun LocalCalendarAccount(modifier: Modifier, viewModel: MainViewModel) {
	val context = LocalContext.current
	LaunchedEffect(key1 = Unit) {
		viewModel.getCalendarAccountInfo(context)
	}
	val hasCalendarAccount by remember {
		derivedStateOf {
			viewModel.calendarList.isNotEmpty()
		}
	}

	if (hasCalendarAccount) {
		LazyColumn(
			modifier = modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {

			items(viewModel.calendarList) {
				CalendarAccountItem(calendarInfoBean = it) { id ->
					viewModel.deleteState = DeleteState.DeleteInfo(id,it.displayName)
				}
			}
		}
	} else {
		NoAccountPage()
	}
}

@Composable
fun CalendarAccountItem(calendarInfoBean: CalendarInfoBean, delete: (Long) -> Unit) {
	Row(modifier = Modifier.fillMaxWidth()) {
		ElevatedCard(
			modifier = Modifier
				.height(60.dp)
				.fillMaxWidth(0.7f),
			colors = CardDefaults.cardColors(containerColor = Color(calendarInfoBean.color))
		) {

			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
				Text(
					text = calendarInfoBean.displayName, modifier = Modifier.padding(start = 24.dp)
				)
			}
		}

		Spacer(modifier = Modifier.width(12.dp))

		ElevatedCard(
			modifier = Modifier
				.height(60.dp)
				.weight(1f)
		) {

			Box(modifier = Modifier
				.fillMaxSize()
				.clickable {
					delete.invoke(calendarInfoBean.id)
				}, contentAlignment = Alignment.Center) {
				Text(text = "删除")
			}
		}
	}
}

@Composable
fun NoAccountPage() {
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		Text(text = "没有账户")
	}
}

@Composable
fun NoPermissionPage(requestPermisison: () -> Unit) {
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		Text(
			text = "点击来授予日历权限", modifier = Modifier.clickable(onClick = requestPermisison)
		)
	}
}

@Composable
fun DeleteDialog(calendarName: String, id: Long, confirm: (Long) -> Unit, cancel: () -> Unit) {
	ElevatedCard(
		modifier = Modifier
			.fillMaxWidth(.7f)
			.wrapContentHeight()
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight()
				.padding(horizontal = 16.dp, vertical = 6.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = "确定删除\"$calendarName\"吗？", fontSize = 16.sp)
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = "别瞎删，删除后对应账户下的日历事件也都会被删除，本地日历账户（我的日历、联系人的重要日期、中国节日）被删除后无法在三星日历里重新创建。")

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				Spacer(modifier = Modifier.weight(1f))
				TextButton(onClick = { confirm.invoke(id) }) {
					Text(text = "确定")
				}
				Spacer(modifier = Modifier.width(12.dp))
				TextButton(onClick = { cancel.invoke() }) {
					Text(text = "取消")
				}
			}
		}

	}
}