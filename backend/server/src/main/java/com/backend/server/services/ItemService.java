package com.backend.server.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.server.models.Account;
import com.backend.server.models.ItemImages;
import com.backend.server.models.Items;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.ItemImagesRepository;
import com.backend.server.repository.ItemsRepository;

@Service
public class ItemService {
    private final ItemsRepository itemsRepository;
    private final ItemImagesRepository itemImagesRepository;
    private final AccountRepository accountRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public ItemService(ItemsRepository itemsRepository, ItemImagesRepository itemImagesRepository, 
                       AccountRepository accountRepository, CloudinaryService cloudinaryService) {
        this.itemsRepository = itemsRepository;
        this.itemImagesRepository = itemImagesRepository;
        this.accountRepository = accountRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Items reportItem(String email, String itemType, String itemName, String description, 
                           String location, String date, String time, String contactPreference, 
                           List<MultipartFile> imageFiles) throws IOException {
        // Get account by email
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();

        // Parse date
        LocalDate dateFound = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Parse time if provided
        LocalTime timeFound = null;
        if (time != null && !time.isEmpty()) {
            timeFound = LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
        }

        // Create new item
        Items item = new Items();
        item.setAccount(account);
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setDateFound(dateFound);
        item.setTimeFound(timeFound);
        item.setItemType(itemType);
        item.setStatus("active");
        item.setContactPreference(contactPreference);
        item.setViewsCount(0);
        item.setMessagesCount(0);
        // createdAt will be set by @PrePersist

        // Save item first to get the ID
        item = itemsRepository.save(item);

        // Upload and save images
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ItemImages> itemImages = new ArrayList<>();
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(file, "item-images");
                    
                    ItemImages itemImage = new ItemImages();
                    itemImage.setItem(item);
                    itemImage.setImageUrl(imageUrl);
                    itemImage.setImageOrder(i + 1);
                    // createdAt will be set by @PrePersist
                    
                    itemImages.add(itemImage);
                }
            }
            itemImagesRepository.saveAll(itemImages);
        }

        return item;
    }

    public List<Items> getUserItems(String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();
        
        // Get all items for this account
        return itemsRepository.findByAccount_AccountId(account.getAccountId());
    }

    public List<ItemImages> getItemImages(Long itemId) {
        return itemImagesRepository.findByItem_ItemIdOrderByImageOrderAsc(itemId);
    }
}

