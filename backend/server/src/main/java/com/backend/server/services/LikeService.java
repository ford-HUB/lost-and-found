package com.backend.server.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.server.models.Account;
import com.backend.server.models.Items;
import com.backend.server.models.Likes;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.ItemsRepository;
import com.backend.server.repository.LikesRepository;

@Service
public class LikeService {
    private final LikesRepository likesRepository;
    private final ItemsRepository itemsRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public LikeService(LikesRepository likesRepository, ItemsRepository itemsRepository, 
                       AccountRepository accountRepository) {
        this.likesRepository = likesRepository;
        this.itemsRepository = itemsRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public boolean toggleLike(String email, Long itemId) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();

        Optional<Items> itemOpt = itemsRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("Item not found with id: " + itemId);
        }
        Items item = itemOpt.get();

        // Check if like already exists
        Optional<Likes> existingLikeOpt = likesRepository.findByAccount_AccountIdAndItem_ItemId(
            account.getAccountId(), itemId);

        if (existingLikeOpt.isPresent()) {
            likesRepository.delete(existingLikeOpt.get());
            return false; // unliked
        } else {
            Likes like = new Likes();
            like.setAccount(account);
            like.setItem(item);
            like.setCreatedAt(LocalDateTime.now());
            likesRepository.save(like);
            return true; // liked
        }
    }

    public Long getLikeCount(Long itemId) {
        return likesRepository.countByItem_ItemId(itemId);
    }

    public boolean isLikedByUser(String email, Long itemId) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            return false;
        }
        Account account = accountOpt.get();
        return likesRepository.existsByAccount_AccountIdAndItem_ItemId(account.getAccountId(), itemId);
    }
}

