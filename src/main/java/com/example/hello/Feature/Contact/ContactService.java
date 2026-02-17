package com.example.hello.Feature.Contact;

import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.ContactMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContactService {
    ContactRepository contactRepository;
    UserRepository userRepository;
    ContactMapper contactMapper;

    @Transactional(readOnly = true)
    public Response<ListResponse<ContactDTO>> getContacts(UUID userId, Pageable pageable) {
        var contacts =  contactRepository.findAllByUser_UserIdOrderByUpdatedAtDesc(userId, pageable);
        log.info("Found contacts successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        contacts.hasNext(),
                        contactMapper.toContactDTOList(contacts.getContent())
                )
        );
    }
    @Transactional
    public Response<ContactDTO> addContact(UUID userId, ContactDTO contactDTO) {
        var user = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        var contact = contactMapper.toContact(contactDTO);
        contact.setUser(user);
        contactRepository.save(contact);
        log.info("Add contact successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                contactMapper.toContactDto(contact)
        );
    }

    @Transactional
    public Response<ContactDTO> updateContact(UUID userId, ContactDTO contactDTO) {
        var contact = contactRepository.findById(contactDTO.getContactId()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.CONTACT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        if(!contact.getUser().getUserId().equals(userId)) {
            throw new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID);
        }
        contactMapper.updateContact(contactDTO, contact);
        contactRepository.save(contact);
        log.info("Update contact successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                contactMapper.toContactDto(contact)
        );
    }

    @Transactional
    public Response<Void> deleteContact(UUID userId, UUID contactId) {
        var contact = contactRepository.findById(contactId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.CONTACT +
                StringApplication.FIELD.NOT_EXIST)
        );
        if(!contact.getUser().getUserId().equals(userId)) {
            log.info("User contact not match in db");
            throw new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID);
        }
        contactRepository.delete(contact);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
