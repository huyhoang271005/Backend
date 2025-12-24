package com.example.hello.Feature.Contact;

import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.ContactMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.ContactRepository;
import com.example.hello.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContactService {
    ContactRepository contactRepository;
    UserRepository userRepository;
    ContactMapper contactMapper;

    public Response<ListResponse<ContactDTO>> getContacts(UUID userId, Pageable pageable) {
        var contacts =  contactRepository.findAllByUser_UserIdOrderByUpdatedAtDesc(userId, pageable);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        contacts.hasNext(),
                        contactMapper.toContactDTOList(contacts.getContent())
                )
        );
    }
    public Response<ContactDTO> addContact(UUID userId, ContactDTO contactDTO) {
        var user = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        var contact = contactMapper.toContact(contactDTO);
        contact.setUser(user);
        contactRepository.save(contact);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                contactMapper.toContactDto(contact)
        );
    }

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
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                contactMapper.toContactDto(contact)
        );
    }

    public Response<Void> deleteContact(UUID userId, UUID contactId) {
        var contact = contactRepository.findById(contactId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.CONTACT +
                StringApplication.FIELD.NOT_EXIST)
        );
        if(!contact.getUser().getUserId().equals(userId)) {
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
