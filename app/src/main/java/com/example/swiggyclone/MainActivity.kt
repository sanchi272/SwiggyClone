package com.example.swiggyclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable

// ---------------- DATA ----------------
data class FoodItem(
    val id: Int,
    val name: String,
    val price: Double,
    val image: Int   // 🔥 resource ID
)

data class CartItem(
    val food: FoodItem,
    var quantity: MutableState<Int> = mutableStateOf(1)
)

data class Order(
    val items: List<CartItem>,
    val total: Double,
    val name: String,
    val phone: String,
    val address: String
)

data class Address(
    val title: String,
    val fullAddress: String,
    val lat: Double,
    val lng: Double
)

// ---------------- MAIN ----------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SwiggyApp() }
    }
}

// ---------------- APP ----------------
@Composable
fun SwiggyApp() {

    val navController = rememberNavController()

    val foodList = listOf(
        FoodItem(1, "Burger", 120.0, R.drawable.burger),
        FoodItem(2, "Pizza", 250.0, R.drawable.pizza),
        FoodItem(3, "Biryani", 180.0, R.drawable.briyani),
        FoodItem(4, "Dosa", 120.0, R.drawable.dosa),
        FoodItem(5, "Idli", 120.0, R.drawable.idlivada),
        FoodItem(6, "Fries", 120.0, R.drawable.fries),
        FoodItem(7, "Noodles", 120.0, R.drawable.noodles),
        FoodItem(8, "KFC Chicken", 120.0, R.drawable.kfcchicken),
        FoodItem(9, "Pasta", 120.0, R.drawable.pasta),
        FoodItem(10, "Cold Coffee", 120.0, R.drawable.coldcoffee)
    )

    val cart = remember { mutableStateListOf<CartItem>() }
    val orders = remember { mutableStateListOf<Order>() }

    Scaffold(
        topBar = { SwiggyTopBar(navController) },
        bottomBar = { BottomNav(navController, cart.size) }
    ) { padding ->

        NavHost(navController, "home", Modifier.padding(padding)) {

            composable("home") {
                HomeScreen(foodList, cart) {
                    navController.navigate("cart")
                }
            }

            composable("cart") {
                CartScreen(cart, navController) {
                    navController.navigate("payment")
                }
            }

            composable("payment") {
                PaymentScreen(cart, orders, navController) {
                    navController.navigate("tracking")
                }
            }

            composable("delivery") {
                DeliveryDetailsScreen(cart, navController)
            }

            composable("tracking") {
                TrackingScreen(navController)
            }

            composable("orders") {
                OrdersScreen(orders, navController)
            }

            composable("search") {
                SearchScreen(foodList, cart, navController)
            }

            composable("map") {
                LocationPickerScreen(navController)
            }

            composable("otp") {
                OTPScreen(navController)
            }

            composable("profileDetails") {
                ProfileDetailsScreen()
            }
        }
    }
}

// ---------------- TOP BAR ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwiggyTopBar(navController: NavHostController) {

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF6D00),
            titleContentColor = Color.White
        ),
        title = {
            Column {
                Text("Home", fontWeight = FontWeight.Bold)
                Text("Mumbai, India", fontSize = 12.sp)
            }
        },
        actions = {

            IconButton(onClick = { navController.navigate("search") }) {
                Icon(Icons.Default.Search, null, tint = Color.White)
            }

            IconButton(onClick = { navController.navigate("profileDetails") }) {
                Icon(Icons.Default.Person, null, tint = Color.White)
            }
        }
    )
}

// ---------------- BOTTOM NAV ----------------
@Composable
fun BottomNav(navController: NavHostController, cartSize: Int) {

    NavigationBar {

        NavigationBarItem(false,
            { navController.navigate("home") },
            { Icon(Icons.Default.Home, null) },
            label = { Text("Home") })

        NavigationBarItem(false,
            { navController.navigate("cart") },
            {
                BadgedBox(
                    badge = {
                        if (cartSize > 0) {
                            Badge { Text("$cartSize") }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                }
            },
            label = { Text("Cart") })

        NavigationBarItem(false,
            { navController.navigate("orders") },
            { Icon(Icons.Default.List, null) },
            label = { Text("Orders") })
    }
}

// ---------------- HOME ----------------
@Composable
fun HomeScreen(foodList: List<FoodItem>, cart: MutableList<CartItem>, goCart: () -> Unit) {

    Column {

        Text(" Menu List ", fontSize = 22.sp, modifier = Modifier.padding(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2)) {

            items(foodList) { item ->

                Card(Modifier.padding(8.dp), shape = RoundedCornerShape(16.dp)) {

                    Column {

                        Image(
                            painter = painterResource(id = item.image),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Column(Modifier.padding(10.dp)) {

                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("₹${item.price}", color = Color.Gray)

                            Button(onClick = {

                                val existing = cart.find { it.food.id == item.id }
                                if (existing != null) {
                                    existing.quantity.value++
                                } else {
                                    cart.add(CartItem(item))
                                }

                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = goCart,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("View Cart (${cart.size})")
        }
    }
}

// ---------------- CART ----------------
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CartScreen(
    cart: MutableList<CartItem>,
    navController: NavHostController,
    goPay: () -> Unit
) {

    val total = cart.sumOf { it.food.price * it.quantity.value }

    Column {

        // 🔙 BACK BAR
        BackTopBar("Cart 🛒", navController)

        Column(Modifier.padding(16.dp)) {

            LazyColumn {

                items(cart, key = { it.food.id }) { item ->

                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                cart.remove(item)
                                true
                            } else false
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),

                        background = {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text("Delete 🗑️")
                            }
                        },

                        dismissContent = {

                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {

                                Row(
                                    Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    Column {
                                        Text(item.food.name, fontWeight = FontWeight.Bold)
                                        Text("₹${item.food.price}")
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        IconButton(onClick = {
                                            if (item.quantity.value > 1)
                                                item.quantity.value--
                                            else
                                                cart.remove(item)
                                        }) {
                                            Icon(Icons.Default.Remove, null)
                                        }

                                        Text("${item.quantity.value}")

                                        IconButton(onClick = {
                                            item.quantity.value++
                                        }) {
                                            Icon(Icons.Default.Add, null)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("Total: ₹$total", fontSize = 18.sp)

            Button(
                onClick = { navController.navigate("delivery") }
            ) {
                Text("Proceed")
            }
        }
    }
}

// ---------------- SEARCH ----------------
@Composable
fun SearchScreen(
    foodList: List<FoodItem>,
    cart: MutableList<CartItem>,
    navController: NavHostController
) {

    var query by remember { mutableStateOf("") }

    // 🔍 FILTER ONLY MATCHING ITEMS
    val filteredList = foodList.filter {
        it.name.contains(query, ignoreCase = true)
    }

    Column {

        // 🔙 BACK BAR
        BackTopBar("Search 🔍", navController)

        Column(Modifier.padding(16.dp)) {

            // 🔍 SEARCH FIELD
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search food...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // ❗ SHOW ONLY WHEN USER TYPES
            if (query.isNotEmpty()) {

                LazyColumn {

                    items(filteredList) { item ->

                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {

                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    // 🖼️ IMAGE
                                    Image(
                                        painter = painterResource(id = item.image),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(end = 10.dp)
                                    )

                                    Column {
                                        Text(item.name, fontWeight = FontWeight.Bold)
                                        Text("₹${item.price}")
                                    }
                                }

                                // ➕ ADD BUTTON
                                Button(onClick = {

                                    val existing = cart.find { it.food.id == item.id }

                                    if (existing != null) {
                                        existing.quantity.value++
                                    } else {
                                        cart.add(CartItem(item))
                                    }

                                }) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- PROFILE ----------------
@Composable
fun ProfileDetailsScreen() {

    Column(Modifier.padding(16.dp)) {

        Text("Profile 👤", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        Text("Name: Saranya")
        Text("Email: saranya@gmail.com")
        Text("Phone: +91 9876543210")
        Text("Address: Mumbai, India")
    }
}

// ---------------- PAYMENT ----------------
@Composable
fun PaymentScreen(
    cart: MutableList<CartItem>,
    orders: MutableList<Order>,
    navController: NavHostController,
    goTrack: () -> Unit
) {

    val total = cart.sumOf { it.food.price * it.quantity.value }

    val name = navController.previousBackStackEntry
        ?.savedStateHandle?.get<String>("name") ?: ""

    val phone = navController.previousBackStackEntry
        ?.savedStateHandle?.get<String>("phone") ?: ""

    val address = navController.previousBackStackEntry
        ?.savedStateHandle?.get<String>("address") ?: ""

    Column {

        BackTopBar("Payment 💳", navController)

        Column(Modifier.padding(16.dp)) {

            Text("Deliver To:", fontWeight = FontWeight.Bold)
            Text(name)
            Text(phone)
            Text(address)

            Spacer(Modifier.height(20.dp))

            Text("Total: ₹$total", fontSize = 18.sp)

            Spacer(Modifier.height(20.dp))

            Button(onClick = {

                orders.add(
                    Order(
                        items = cart.toList(),
                        total = total,
                        name = name,
                        phone = phone,
                        address = address
                    )
                )

                cart.clear()
                goTrack()

            }, modifier = Modifier.fillMaxWidth()) {

                Text("Confirm Payment")
            }
        }
    }
}

// ---------------- TRACKING ----------------
@Composable
fun TrackingScreen(navController: NavHostController) {

    val start = LatLng(19.0760, 72.8777)
    val end = LatLng(19.0910, 72.8650)

    var current by remember { mutableStateOf(start) }

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(start, 13f)
    }

    LaunchedEffect(Unit) {
        for (i in 1..100) {
            val lat = start.latitude + (end.latitude - start.latitude) * i / 100
            val lng = start.longitude + (end.longitude - start.longitude) * i / 100
            current = LatLng(lat, lng)
            delay(150)
        }
    }

    Column {

        // 🔙 BACK BAR
        BackTopBar("Tracking 🗺️", navController)

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            cameraPositionState = camera
        ) {
            Marker(MarkerState(current))
            Marker(MarkerState(end))
            Polyline(listOf(start, end))
        }
    }
}

// ---------------- ORDERS ----------------

@Composable
fun OrdersScreen(
    orders: List<Order>,
    navController: NavHostController
) {

    Column {

        BackTopBar("My Orders 📦", navController)

        LazyColumn {

            items(orders) { order ->

                Card(Modifier.padding(8.dp)) {

                    Column(Modifier.padding(10.dp)) {

                        Text("Total: ₹${order.total}", fontWeight = FontWeight.Bold)

                        Text("Name: ${order.name}")
                        Text("Phone: ${order.phone}")
                        Text("Address: ${order.address}")
                    }
                }
            }
        }
    }
}

// ----------------- BackTopBar --------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, navController: NavHostController) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

// ---------------- DeliveryDetails --------------

@Composable
fun DeliveryDetailsScreen(
    cart: MutableList<CartItem>,
    navController: NavHostController
) {

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var manualAddress by remember { mutableStateOf("") }

    // 🏠 MULTIPLE ADDRESSES
    val addressList = remember { mutableStateListOf<Address>() }
    var selectedAddress by remember { mutableStateOf<Address?>(null) }

    // 📍 GET LOCATION FROM MAP
    val lat = navController.currentBackStackEntry
        ?.savedStateHandle?.get<Double>("lat")

    val lng = navController.currentBackStackEntry
        ?.savedStateHandle?.get<Double>("lng")

    // 🔁 ADD NEW ADDRESS FROM MAP
    LaunchedEffect(lat, lng) {
        if (lat != null && lng != null) {

            val newAddress = Address(
                title = "Selected Location",
                fullAddress = "Lat: $lat, Lng: $lng",
                lat = lat,
                lng = lng
            )

            // Avoid duplicates
            if (!addressList.contains(newAddress)) {
                addressList.add(newAddress)
            }
        }
    }

    Column {

        // 🔙 BACK BAR
        BackTopBar("Delivery Details 🚚", navController)

        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // 👤 NAME
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // 📱 PHONE
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // 🏠 MANUAL ADDRESS
            OutlinedTextField(
                value = manualAddress,
                onValueChange = { manualAddress = it },
                label = { Text("Enter Address Manually") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(15.dp))

            // 📍 ADD NEW ADDRESS BUTTON
            Button(
                onClick = { navController.navigate("map") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick Location from Map 📍")
            }

            Spacer(Modifier.height(15.dp))

            // 🏠 SAVED ADDRESSES LIST
            if (addressList.isNotEmpty()) {

                Text("Saved Addresses", fontWeight = FontWeight.Bold)

                LazyColumn {

                    items(addressList) { addr ->

                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { selectedAddress = addr }
                        ) {

                            Column(Modifier.padding(10.dp)) {

                                Text(addr.title, fontWeight = FontWeight.Bold)
                                Text(addr.fullAddress)

                                if (selectedAddress == addr) {
                                    Text("Selected ✅", color = Color.Green)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 👉 CONTINUE BUTTON
            Button(
                onClick = {

                    val finalAddress = selectedAddress?.fullAddress
                        ?: manualAddress

                    if (name.isNotEmpty() &&
                        phone.isNotEmpty() &&
                        finalAddress.isNotEmpty()
                    ) {

                        // 🔥 PASS DATA
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("name", name)

                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("phone", phone)

                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("address", finalAddress)

                        // 📱 GO TO OTP
                        navController.navigate("otp")

                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

// ------------- LocationPicker ------------

@Composable
fun LocationPickerScreen(navController: NavHostController) {

    val default = LatLng(19.0760, 72.8777) // Mumbai

    var selected by remember { mutableStateOf(default) }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(default, 14f)
    }

    Column {

        BackTopBar("Select Location 📍", navController)

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            cameraPositionState = cameraState,
            onMapClick = {
                selected = it
            }
        ) {
            Marker(state = MarkerState(position = selected))
        }

        Button(
            onClick = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("lat", selected.latitude)

                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("lng", selected.longitude)

                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Confirm Location")
        }
    }
}

// ------------- OTP -------------

@Composable
fun OTPScreen(navController: NavHostController) {

    var otp by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        BackTopBar("OTP Verification 📱", navController)

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("Enter OTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                // simple validation
                if (otp.length == 4) {
                    navController.navigate("payment")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify OTP")
        }
    }
}