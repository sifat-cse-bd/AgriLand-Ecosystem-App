package com.example.villageconnect.models
object DropDownData {

    // District list
    val districts = listOf(
        DropDownItem(1, "Dhaka"),
        DropDownItem(2, "Gazipur"),
        DropDownItem(3, "Kishoreganj"),
        DropDownItem(4, "Manikganj"),
        DropDownItem(5, "Munshiganj"),
        DropDownItem(6, "Narayanganj"),
        DropDownItem(7, "Narsingdi"),
        DropDownItem(8, "Rajbari"),
        DropDownItem(9, "Shariatpur"),
        DropDownItem(10, "Tangail"),
        DropDownItem(11, "Faridpur"),
        DropDownItem(12, "Gopalganj"),
        DropDownItem(13, "Madaripur"),
        // Chittagong
        DropDownItem(14, "Chattogram"),
        DropDownItem(15, "Cox's Bazar"),
        DropDownItem(16, "Brahmanbaria"),
        DropDownItem(17, "Chandpur"),
        DropDownItem(18, "Cumilla"),
        DropDownItem(19, "Feni"),
        DropDownItem(20, "Khagrachhari"),
        DropDownItem(21, "Lakshmipur"),
        DropDownItem(22, "Noakhali"),
        DropDownItem(23, "Rangamati"),
        DropDownItem(24, "Bandarban"),
        // Rajshahi
        DropDownItem(25, "Rajshahi"),
        DropDownItem(26, "Bogura"),
        DropDownItem(27, "Chapai Nawabganj"),
        DropDownItem(28, "Joypurhat"),
        DropDownItem(29, "Naogaon"),
        DropDownItem(30, "Natore"),
        DropDownItem(31, "Pabna"),
        DropDownItem(32, "Sirajganj"),
        // Khulna
        DropDownItem(33, "Khulna"),
        DropDownItem(34, "Bagerhat"),
        DropDownItem(35, "Chuadanga"),
        DropDownItem(36, "Jashore"),
        DropDownItem(37, "Jhenaidah"),
        DropDownItem(38, "Kushtia"),
        DropDownItem(39, "Magura"),
        DropDownItem(40, "Meherpur"),
        DropDownItem(41, "Narail"),
        DropDownItem(42, "Satkhira"),
        // Barisal
        DropDownItem(43, "Barishal"),
        DropDownItem(44, "Barguna"),
        DropDownItem(45, "Bhola"),
        DropDownItem(46, "Jhalokati"),
        DropDownItem(47, "Patuakhali"),
        DropDownItem(48, "Pirojpur"),
        // Sylhet
        DropDownItem(49, "Sylhet"),
        DropDownItem(50, "Habiganj"),
        DropDownItem(51, "Moulvibazar"),
        DropDownItem(52, "Sunamganj"),
        // Rangpur
        DropDownItem(53, "Rangpur"),
        DropDownItem(54, "Dinajpur"),
        DropDownItem(55, "Gaibandha"),
        DropDownItem(56, "Kurigram"),
        DropDownItem(57, "Lalmonirhat"),
        DropDownItem(58, "Nilphamari"),
        DropDownItem(59, "Panchagarh"),
        DropDownItem(60, "Thakurgaon"),
        // Mymensingh
        DropDownItem(61, "Mymensingh"),
        DropDownItem(62, "Jamalpur"),
        DropDownItem(63, "Netrokona"),
        DropDownItem(64, "Sherpur")
    )

    // Map of district ID to upazilas
    val upazilas = mapOf(
        1 to listOf("Adabor","Badda","Bangshal","Cantonment","Chawkbazar","Dakshin Khan","Demra","Dhanmondi","Dohar","Gendaria","Gulshan","Hazaribagh","Jatrabari","Kadamtali","Kafrul","Kalabagan","Kamrangirchar","Keraniganj","Khilgaon","Khilkhet","Kotwali","Lalbagh","Mirpur","Mohammadpur","Motijheel","Nawabganj","New Market","Pallabi","Paltan","Ramna","Rayer Bazar","Sabujbagh","Shah Ali","Shahjahanpur","Sher-e-Bangla Nagar","Shyampur","Sutrapur","Tejgaon","Turag","Uttar Khan","Uttara"),
        2 to listOf("Gazipur Sadar","Tongi","Kaliakair","Kaliganj","Kapasia","Sreepur"),
        3 to listOf("Kishoreganj Sadar","Austagram","Bajitpur","Bhairab","Hossainpur","Itna","Karimganj","Katiadi","Kuliarchar","Mithamain","Nikli","Pakundia","Tarail"),
        4 to listOf("Manikganj Sadar","Daulatpur","Ghior","Harirampur","Saturia","Shivalaya","Singair"),
        5 to listOf("Munshiganj Sadar","Gazaria","Lohajang","Sirajdikhan","Sreenagar","Tongibari"),
        6 to listOf("Narayanganj Sadar","Araihazar","Bandar","Rupganj","Sonargaon"),
        7 to listOf("Narsingdi Sadar","Belabo","Monohardi","Palash","Raipura","Shibpur"),
        8 to listOf("Rajbari Sadar","Baliakandi","Goalanda","Kalukhali","Pangsha"),
        9 to listOf("Shariatpur Sadar","Bhedarganj","Damudya","Gosairhat","Naria","Vijaynagor","Zanjira"),
        10 to listOf("Tangail Sadar","Basail","Bhuapur","Delduar","Dhanbari","Ghatail","Gopalpur","Kalihati","Madhupur","Mirzapur","Nagarpur","Sakhipur"),
        11 to listOf("Faridpur Sadar","Alfadanga","Bhanga","Boalmari","Charbhadrasan","Madhukhali","Nagarkanda","Sadarpur","Saltha"),
        12 to listOf("Gopalganj Sadar","Kashiani","Kotalipara","Muksudpur","Tungipara"),
        13 to listOf("Madaripur Sadar","Kalkini","Rajoir","Shibchar"),
        // Chittagong
        14 to listOf("Anwara","Banshkhali","Boalkhali","Chandanaish","Fatikchhari","Hathazari","Karnaphuli","Lohagara","Mirsharai","Patiya","Rangunia","Raozan","Sandwip","Satkania","Sitakunda","Chattogram Sadar"),
        15 to listOf("Cox's Bazar Sadar","Chakaria","Kutubdia","Maheshkhali","Pekua","Ramu","Teknaf","Ukhia"),
        16 to listOf("Brahmanbaria Sadar","Akhaura","Ashuganj","Bancharampur","Bijoynagar","Kasba","Nabinagar","Nasirnagar","Sarail"),
        17 to listOf("Chandpur Sadar","Faridganj","Haimchar","Haziganj","Kachua","Matlab Dakshin","Matlab Uttar","Shahrasti"),
        18 to listOf("Comilla Sadar","Barura","Brahmanpara","Burichang","Chandina","Chauddagram","Daudkandi","Debidwar","Homna","Laksam","Lalmai","Meghna","Monohorgonj","Muradnagar","Nangalkot","Titas","Comilla Sadar Dakshin"),
        19 to listOf("Feni Sadar","Chhagalnaiya","Daganbhuiyan","Fulgazi","Parshuram","Sonagazi"),
        20 to listOf("Khagrachhari Sadar","Dighinala","Guimara","Lakshmichhari","Mahalchhari","Manikchhari","Matiranga","Panchhari","Ramgarh"),
        21 to listOf("Lakshmipur Sadar","Kamalnagar","Raipur","Ramganj","Ramgati"),
        22 to listOf("Noakhali Sadar","Begumganj","Chatkhil","Companiganj","Hatiya","Kabirhat","Senbagh","Sonaimuri","Subarnachar"),
        23 to listOf("Rangamati Sadar","Bagaichhari","Barkal","Belaichhari","Juraichhari","Kaptai","Kaukhali","Langadu","Naniarchar","Rajasthali"),
        24 to listOf("Bandarban Sadar","Alikadam","Lama","Naikhongchhari","Rowangchhari","Ruma","Thanchi")

    )

    // Roles
    val roles = listOf(
        DropDownItem(1, "Farmer"),
        DropDownItem(2, "Landowner"),
        DropDownItem(3, "Agri-Merchant")
    )
}